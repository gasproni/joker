package com.asprotunity.joker.internal.protocol;

import com.asprotunity.joker.proxy.ServiceAddress;
import com.asprotunity.joker.proxy.ServiceProxy;
import com.google.gson.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class JsonServiceProxyAdapter implements
        JsonDeserializer<ServiceProxy<?>>,
        JsonSerializer<ServiceProxy<?>> {

    private ServiceProxyMaker serviceProxyMaker;

    public JsonServiceProxyAdapter(ServiceProxyMaker serviceProxyMaker) {
        this.serviceProxyMaker = serviceProxyMaker;
    }

    @Override
    public JsonElement serialize(ServiceProxy<?> serviceProxy,
                                 Type type,
                                 JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(serviceProxy.address());
    }

    @Override
    public ServiceProxy<?>
    deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        ServiceAddress address = jsonDeserializationContext.deserialize(jsonElement, ServiceAddress.class);
        return serviceProxyMaker.make(address, (Class<?>) (parameterizedType.getActualTypeArguments()[0]));
    }
}
