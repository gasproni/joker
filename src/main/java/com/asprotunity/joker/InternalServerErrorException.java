package com.asprotunity.joker;

import org.eclipse.jetty.http.HttpStatus;

public class InternalServerErrorException extends RemoteException {

    public InternalServerErrorException(String message) {
        super(message);
    }

    @Override
    public int getErrorCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR_500;
    }
}
