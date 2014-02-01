package com.asprotunity.joker;

import org.eclipse.jetty.http.HttpStatus;

public class NotFoundException extends RemoteException {

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public int getErrorCode() {
        return HttpStatus.NOT_FOUND_404;
    }
}
