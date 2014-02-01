package com.asprotunity.joker.internal.protocol;

import com.asprotunity.joker.proxy.ServiceAddress;

public interface ServiceCaller {
    Object call(Object[] params, String methodName, ServiceAddress serviceAddress, Class<?> returnType);
}
