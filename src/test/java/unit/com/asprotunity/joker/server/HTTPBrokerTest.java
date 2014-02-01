package com.asprotunity.joker.server;

import com.asprotunity.joker.proxy.ServiceProxy;
import com.asprotunity.joker.proxy.InterfaceHasMethodsWithSimilarName;
import com.asprotunity.joker.testdata.*;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class HTTPBrokerTest {

    private HTTPBroker broker;
    private String serviceName;
    private Service service;


    @Before
    public void setUp() throws Exception {
        broker = new HTTPBroker(0);
        serviceName = "serviceName";
        service = new Service();
        broker.start();
    }

    @Test
    public void portIsSetCorrectlyWhenZeroUsed() {
        assertThat(broker.getPort(), not(0));
    }


    @Test
    public void registersServiceWithAGivenNameAndReturnsProxy() {
        ServiceProxy<ServiceInterface> proxy =
                broker.registerService(serviceName, service,
                        ServiceInterface.class);

        assertThat(proxy.service(), is((ServiceInterface)service));
    }


    @Test(expected = ServiceAlreadyRegisteredException.class)
    public void registrationFailsWhenServiceWithSameNameAlreadyRegistered() {
        broker.registerService(serviceName, service,
                ServiceInterface.class);
        broker.registerService(serviceName, service,
                ServiceInterface.class);
    }


    @Test(expected = BrokerNotStartedException.class)
    public void registrationFailsWhenNotStarted() {
        broker.stop();
        broker.join();
        broker.registerService(serviceName, service,
                ServiceInterface.class);
    }


    @Test(expected = IllegalArgumentException.class)
    public void registrationFailsIfClassParameterIsNotAnInterface() {
        broker.registerService(serviceName, service,
                Service.class);
    }


    @Test(expected = InterfaceHasMethodsWithSimilarName.class)
    public void
    registrationFailsIfInterfaceHasOverloadedMethods() {
        broker.registerService(serviceName, new ServiceWithOverloading(),
                ServiceInterfaceWithOverloading.class);
    }


    @Test(expected = InterfaceHasMethodsWithSimilarName.class)
    public void
    registrationFailsIfInterfaceHasMethodsWithNamesDifferingOnlyInCase() {
        broker.registerService(serviceName, new ServiceWithMethodNamesDifferingInCase(),
                ServiceInterfaceWithMethodNamesDifferingInCase.class);
    }

}
