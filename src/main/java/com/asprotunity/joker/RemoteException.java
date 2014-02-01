package com.asprotunity.joker;

public abstract class RemoteException extends RuntimeException {

    public RemoteException(String message) {
        super(message);
    }

    public abstract int getErrorCode();
}


