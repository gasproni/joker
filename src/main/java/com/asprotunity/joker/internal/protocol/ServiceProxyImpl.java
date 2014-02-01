package com.asprotunity.joker.internal.protocol;

import com.asprotunity.joker.proxy.InterfaceHasMethodsWithSimilarName;
import com.asprotunity.joker.proxy.ServiceAddress;
import com.asprotunity.joker.proxy.ServiceProxy;

import java.lang.reflect.Method;
import java.util.HashSet;

public class ServiceProxyImpl<T> implements ServiceProxy<T> {
    protected final T service;
    protected ServiceAddress address;

    public final Class<T> interfaceClass;

    public ServiceProxyImpl(ServiceAddress address, T service, Class<T> interfaceClass) {
        checkIsAnInterface(interfaceClass);
        checkHasNoMethodWithSimilarNames(interfaceClass);
        this.address = address;
        this.service = service;
        this.interfaceClass = interfaceClass;
    }

    @Override
    public ServiceAddress address() {
        return address;
    }

    @Override
    public T service() {
        return service;
    }

    private void checkIsAnInterface(Class<T> interfaceClass) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException(String.format("'%s' must " +
                    "be an interface type",
                    interfaceClass.getName()));
        }
    }

    private void checkHasNoMethodWithSimilarNames(Class<T> interfaceClass) {
        HashSet<String> methodNames = new HashSet<>();
        for (Method method : interfaceClass.getMethods()) {
            if (!methodNames.add(method.getName().toLowerCase())) {
                throw new InterfaceHasMethodsWithSimilarName(String.format
                        ("Interface '%s' has methods with name similar to method '%s'",
                                interfaceClass.getName(), method.getName()));
            }
        }
    }

}
