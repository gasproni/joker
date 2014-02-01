package com.asprotunity.joker.internal.protocol;

public class ExceptionWrapper {
    public final String message;
    public final String stackTrace;

    public ExceptionWrapper(String message, String stackTrace) {
        this.message = message;
        this.stackTrace = stackTrace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExceptionWrapper that = (ExceptionWrapper) o;

        return message.equals(that.message) &&
                stackTrace.equals(that.stackTrace);
    }

    @Override
    public int hashCode() {
        int result = message.hashCode();
        result = 31 * result + stackTrace.hashCode();
        return result;
    }
}
