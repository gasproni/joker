package com.asprotunity.joker;

import org.eclipse.jetty.http.HttpStatus;

public class BadRequestException extends RemoteException {

    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public int getErrorCode() {
        return HttpStatus.BAD_REQUEST_400;
    }
}
