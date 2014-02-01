package com.asprotunity.joker.server;

public class ServiceAlreadyRegisteredException extends RuntimeException {
    public ServiceAlreadyRegisteredException(String message) {
        super(message);
    }
}
