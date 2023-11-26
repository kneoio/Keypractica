package io.kneo.core.service.exception;

public class ServiceException extends RuntimeException {

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(Throwable failure) {
        super(failure);
    }

    public String getDeveloperMessage() {
        return getMessage();
    }
}
