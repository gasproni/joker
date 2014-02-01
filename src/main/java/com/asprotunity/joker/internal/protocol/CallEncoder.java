package com.asprotunity.joker.internal.protocol;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

public interface CallEncoder {

    String encode(Object[] params);

    String encode(Object object);

    <T> T decode(String callResultString, Class<T> resultClass);

    Object[] decode(String callParametersString, Type[] parameterTypes);

    String contentType();

    Charset charset();
}
