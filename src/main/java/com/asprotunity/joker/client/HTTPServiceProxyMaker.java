package com.asprotunity.joker.client;

import com.asprotunity.joker.internal.protocol.HTTPServiceCaller;
import com.asprotunity.joker.proxy.ServiceAddress;
import com.asprotunity.joker.proxy.ServiceProxy;
import com.asprotunity.joker.internal.protocol.ServiceProxyMaker;

public class HTTPServiceProxyMaker {
    ServiceProxyMaker proxyMaker = new ServiceProxyMaker(new
            HTTPServiceCaller());
    public <T> ServiceProxy<T> make(final ServiceAddress serviceAddress,
                                    final Class<T> interfaceClass) {
        return proxyMaker.make(serviceAddress, interfaceClass);
    }
}
