package com.asprotunity.joker.proxy;

public interface ServiceProxy<T> {
    ServiceAddress address();

    T service();
}
