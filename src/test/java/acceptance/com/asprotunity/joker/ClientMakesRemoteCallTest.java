package com.asprotunity.joker;

import com.asprotunity.joker.client.HTTPServiceProxyMaker;
import com.asprotunity.joker.proxy.ServiceAddress;
import com.asprotunity.joker.proxy.ServiceProxy;
import com.asprotunity.joker.server.HTTPBroker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClientMakesRemoteCallTest {

    private HTTPBroker remoteBroker;
    private int port;
    private String serviceName;
    private HTTPServiceProxyMaker proxyMaker;

    static public class Result {
        public final String param1;
        public final int param2;

        public Result(String param1, int param2) {
            this.param1 = param1;
            this.param2 = param2;
        }
    }

    public static interface LocalService {
        Result call(int param2);

        void anotherCall();
    }

    public static class LocalServiceImpl implements LocalService {

        Result result = null;

        @Override
        public Result call(int value) {
            result = new Result("", value);
            return result;
        }

        @Override
        public void anotherCall() {
        }
    }

    public static interface RemoteService {
        public void call();

        public Result callWithResult(String param1, int param2);

        public Result callWithProxy(ServiceProxy<LocalService> service1, int param2);
    }

    AtomicBoolean called;

    static public class RemoteServiceImpl implements RemoteService {
        public final AtomicBoolean called;

        public RemoteServiceImpl(AtomicBoolean called) {

            this.called = called;
        }

        @Override
        public void call() {
            this.called.set(true);
        }

        @Override
        public Result callWithResult(String param1, int param2) {
            return new Result(param1, param2);
        }

        @Override
        public Result callWithProxy(ServiceProxy<LocalService> service1, int param2) {
            return service1.service().call(param2);
        }

        public int internalPublicCall(int param) {
            return param;
        }
    }

    static public interface LocalInterfaceToTryToAccessInternalsOfRemoteServiceImpl {
        public int internalPublicCall(int param);
    }

    static public class ThrowingRemoteService implements RemoteService {

        private RuntimeException toThrow;

        public ThrowingRemoteService(RuntimeException toThrow) {

            this.toThrow = toThrow;
        }

        @Override
        public void call() {
            throw toThrow;
        }

        @Override
        public Result callWithResult(String param1, int param2) {
            throw toThrow;
        }

        @Override
        public Result callWithProxy(ServiceProxy<LocalService> service1, int param2) {
            throw toThrow;
        }
    }


    @Before
    public void setUp() {
        remoteBroker = new HTTPBroker(0);
        remoteBroker.start();
        port = remoteBroker.getPort();
        serviceName = "serviceName";
        called = new AtomicBoolean(false);
        RemoteService service = new RemoteServiceImpl(called);
        remoteBroker.registerService(serviceName, service, RemoteService.class);
        proxyMaker = new HTTPServiceProxyMaker();
    }

    @After
    public void tearDown() {
        remoteBroker.stop();
        remoteBroker.join();
    }


    @Test
    public void performsRemoteCallWithNoArgsAndVoidResultWhenNamedServiceExists() {

        ServiceAddress serviceAddress = new ServiceAddress("localhost", port, serviceName);
        RemoteService localProxy = proxyMaker.make(serviceAddress,
                RemoteService.class).service();
        localProxy.call();

        assertThat(called.get(), is(true));
    }

    @Test
    public void performsRemoteCallWithArgsAndResultWhenNamedServiceExists() {

        ServiceAddress serviceAddress = new ServiceAddress("localhost", port, serviceName);
        RemoteService remoteProxy = proxyMaker.make(serviceAddress,
                RemoteService.class).service();

        String param1 = "param1";
        int param2 = 2;
        Result result = remoteProxy.callWithResult(param1, param2);

        assertThat(result.param1, equalTo(param1));
        assertThat(result.param2, equalTo(param2));
    }


    @Test(expected = BadRequestException.class)
    public void cannotAccessInternalMethodsOfRemoteServiceImplementation() {

        ServiceAddress serviceAddress = new ServiceAddress("localhost", port, serviceName);

        LocalInterfaceToTryToAccessInternalsOfRemoteServiceImpl remoteProxy =
                proxyMaker.make(serviceAddress,
                LocalInterfaceToTryToAccessInternalsOfRemoteServiceImpl.class).service();

        remoteProxy.internalPublicCall(10);
    }


    @Test
    public void callsServicePassedAsParameter() {


        ServiceAddress remoteServiceAddress = new ServiceAddress("localhost", port, serviceName);

        RemoteService remoteService = proxyMaker.make(remoteServiceAddress,
                RemoteService.class).service();

        int port1 = port + 1;
        HTTPBroker localBroker = new HTTPBroker(port1);
        localBroker.start();

        LocalServiceImpl localService = new LocalServiceImpl();
        ServiceProxy<LocalService> service1Proxy = localBroker.registerService("serviceName1",
                localService, LocalService.class);


        Result result = remoteService.callWithProxy(service1Proxy, 10);

        assertThat(result.param1, is(localService.result.param1));
        assertThat(result.param2, is(localService.result.param2));

        localBroker.stop();
        localBroker.join();
    }


    @Test(expected = BadRequestException.class)
    public void throwsExceptionWhenWrongMethodCalled() {

        ServiceAddress serviceAddress = new ServiceAddress("localhost", port, serviceName);
        LocalService remoteProxy = proxyMaker.make(serviceAddress,
                LocalService.class).service();

        remoteProxy.anotherCall();
    }


    @Test(expected = InternalServerErrorException.class)
    public void
    throwsInternalServerErrorExceptionWhenWrongServerThrowsUnknownException() {

        String throwingRemoteServiceName =
                registerThrowingRemoteService(new RuntimeException("a message"));

        ServiceAddress serviceAddress = new ServiceAddress("localhost", port,
                throwingRemoteServiceName);

        RemoteService remoteProxy = proxyMaker.make(serviceAddress,
                RemoteService.class).service();

        remoteProxy.call();
    }


    @Test(expected = NotFoundException.class)
    public void
    rethrowsExceptionIfSubclassOfRemoteServerException() {

        String throwingRemoteServiceName =
                registerThrowingRemoteService(new NotFoundException("some message"));

        ServiceAddress serviceAddress = new ServiceAddress("localhost", port,
                throwingRemoteServiceName);

        RemoteService remoteProxy = proxyMaker.make(serviceAddress,
                RemoteService.class).service();

        remoteProxy.call();
    }


    private String registerThrowingRemoteService(RuntimeException toThrow) {

        String throwingRemoteServiceName = "throwingRemoteServiceName";
        remoteBroker.registerService(throwingRemoteServiceName,
                new ThrowingRemoteService(toThrow),
                RemoteService.class);
        return throwingRemoteServiceName;
    }
}
