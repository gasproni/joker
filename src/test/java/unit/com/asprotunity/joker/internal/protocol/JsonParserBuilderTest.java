package com.asprotunity.joker.internal.protocol;

import com.asprotunity.joker.proxy.ServiceAddress;
import com.asprotunity.joker.proxy.ServiceProxy;
import com.asprotunity.joker.testdata.ServiceInterface;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


public class JsonParserBuilderTest {


    private ServiceInterface service;
    private ServiceAddress serviceAddress;
    private String expectedJson;
    private Gson gson;
    private Mockery context;

    @Before
    public void setUp() {

        context = new JUnit4Mockery();
        service = context.mock(ServiceInterface.class);
        serviceAddress = new
                ServiceAddress("hostName", 1234, "serviceName");
        expectedJson = "{\"hostName\":\"hostName\"," +
                "\"port\":1234,\"serviceName\":\"serviceName\"}";
        gson = JsonParserBuilder.build(
                new ServiceProxyMaker(new ServiceCaller() {
                    @Override
                    public Object call(Object[] args,
                                       String methodName,
                                       ServiceAddress serviceAddress, Class<?> returnType) {
                        service.call((int) (args[0]));
                        return null;
                    }
                }));
    }

    @Test
    public void serializesServiceProxyCorrectly() {

        ServiceProxy<ServiceInterface> serviceProxy = new ServiceProxyImpl<>(serviceAddress, service,
                ServiceInterface.class);
        String proxyString = gson.toJson(serviceProxy);

        assertThat(proxyString, equalTo(expectedJson));
    }


    @Test
    public void deserializesProxyCorrectly() {

        Type typeToken =
                new TypeToken<ServiceProxy<ServiceInterface>>() {
                }.getType();

        ServiceProxyImpl<ServiceInterface> deserializedProxy = gson.fromJson
                (expectedJson, typeToken);

        assertProxyPointsToCorrectService(deserializedProxy);

    }

    private void assertProxyPointsToCorrectService(ServiceProxy<ServiceInterface> deserializedProxy) {
        assertThat(deserializedProxy.address(), equalTo(serviceAddress));

        final int callParameter = 10;
        context.checking(new Expectations() {{
            oneOf(service).call(callParameter);
        }});
        deserializedProxy.service().call(callParameter);
        context.assertIsSatisfied();
    }

}
