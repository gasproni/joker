package com.asprotunity.joker.internal.protocol;

import com.asprotunity.joker.proxy.ServiceProxy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonParserBuilder {

    static public Gson build(ServiceProxyMaker serviceProxyMaker) {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeHierarchyAdapter(ServiceProxy.class,
                new JsonServiceProxyAdapter(serviceProxyMaker)).disableHtmlEscaping();

        return builder.create();
    }
}
