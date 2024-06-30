package io.kneo.core.service.exception;

public class DataValidationException extends RuntimeException {

    public DataValidationException(String msg) {
        super(msg);
    }

    public DataValidationException(Throwable failure) {
        super(failure);
    }

    public String getDeveloperMessage() {
        return getMessage();
    }
}
