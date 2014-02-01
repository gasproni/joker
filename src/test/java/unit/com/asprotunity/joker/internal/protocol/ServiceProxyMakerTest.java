package com.asprotunity.joker.internal.protocol;

import com.asprotunity.joker.proxy.InterfaceHasMethodsWithSimilarName;
import com.asprotunity.joker.proxy.ServiceAddress;
import com.asprotunity.joker.testdata.Service;
import com.asprotunity.joker.testdata.ServiceInterface;
import com.asprotunity.joker.testdata.ServiceInterfaceWithMethodNamesDifferingInCase;
import com.asprotunity.joker.testdata.ServiceInterfaceWithOverloading;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;


public class ServiceProxyMakerTest {

    private ServiceAddress serviceAddress;
    private Mockery context;
    private ServiceCaller serviceCaller;
    private ServiceProxyMaker proxyMaker;


    @Before
    public void setUp() throws Exception {
        serviceAddress = new ServiceAddress("hostName", 1234, "serviceName");
        context = new JUnit4Mockery();
        serviceCaller = context.mock(ServiceCaller.class);
        proxyMaker = new ServiceProxyMaker(serviceCaller);
    }

    @Test
    public void generatesProxyCorrectly() {

        final int value = 10;
        final String expectedMethodName = ServiceInterface.class.getMethods()[0]
                .getName();
        final Object[] args = {value};

        context.checking(new Expectations() {{
            oneOf(serviceCaller).call(args, expectedMethodName, serviceAddress, Void.TYPE);
            will(returnValue(null));
        }});

        ServiceInterface ft = proxyMaker.make(serviceAddress,
                ServiceInterface.class).service();

        ft.call(value);
        context.assertIsSatisfied();
    }

    @Test(expected = IllegalArgumentException.class)
    public void registrationFailsIfClassParameterIsNotAnInterface() {
        proxyMaker.make(serviceAddress, Service.class);
    }


    @Test(expected = InterfaceHasMethodsWithSimilarName.class)
    public void
    registrationFailsIfInterfaceHasOverloadedMethods() {
        proxyMaker.make(serviceAddress, ServiceInterfaceWithOverloading.class);
    }

    @Test(expected = InterfaceHasMethodsWithSimilarName.class)
    public void
    registrationFailsIfInterfaceHasMethodsWithNamesDifferingInCase() {
        proxyMaker.make(serviceAddress, ServiceInterfaceWithMethodNamesDifferingInCase.class);
    }
}
