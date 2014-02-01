package com.asprotunity.joker.internal.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class JsonCallEncoder implements CallEncoder {

    private Gson gson;
    private final JsonParser jsonParser;

    public JsonCallEncoder(Gson gson) {
        this.gson = gson;
        jsonParser = new JsonParser();
    }

    @Override
    public String encode(Object[] callParameters) {
        if (callParameters == null) {
            return "";
        }
        return gson.toJson(callParameters);
    }

    @Override
    public String encode(Object object) {
        return gson.toJson(object);
    }

    @Override
    public <T> T decode(String jsonString, Class<T> resultClass) {
        if (resultClass == Void.TYPE) {
            return null;
        }
        return gson.fromJson(jsonString, resultClass);
    }

    @Override
    public Object[] decode(String callParametersString, Type[] parameterTypes) {
        if (callParametersString.isEmpty() && parameterTypes.length == 0) {
            return new Object[0];
        }
        JsonArray parametersArray = jsonParser.parse(callParametersString).getAsJsonArray();
        Object[] result = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; ++i) {
            result[i] = gson.fromJson(parametersArray.get(i),
                    TypeToken.get((Type) parameterTypes[i]).getType());
        }

        return result;
    }

    @Override
    public String contentType() {
        return "application/json;charset=" + charset().name();
    }

    @Override
    public Charset charset() {
        return StandardCharsets.UTF_8;
    }
}
