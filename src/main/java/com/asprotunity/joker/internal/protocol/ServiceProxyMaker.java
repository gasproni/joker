package com.asprotunity.joker.internal.protocol;

import com.asprotunity.joker.proxy.ServiceAddress;
import com.asprotunity.joker.proxy.ServiceProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ServiceProxyMaker {

    private ServiceCaller serviceCaller;

    public ServiceProxyMaker(ServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
    }

    @SuppressWarnings(value = "unchecked")
    public <T> ServiceProxy<T> make(final ServiceAddress serviceAddress, final Class<T> interfaceClass) {

        final T proxy = (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                new RemoteServiceInvocationHandler(serviceCaller,
                        serviceAddress));

        return new ServiceProxyImpl<>(serviceAddress, proxy, interfaceClass);
    }

    public static class RemoteServiceInvocationHandler implements InvocationHandler {

        private volatile ServiceCaller serviceCaller;
        private volatile ServiceAddress serviceAddress;

        public RemoteServiceInvocationHandler(ServiceCaller serviceCaller, ServiceAddress serviceAddress) {
            this.serviceCaller = serviceCaller;
            this.serviceAddress = serviceAddress;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Object result = serviceCaller.call(args, method.getName(),
                    serviceAddress, method.getReturnType());

            if (method.getReturnType() == Void.TYPE) {
                return null;
            }

            return result;
        }
    }
}


