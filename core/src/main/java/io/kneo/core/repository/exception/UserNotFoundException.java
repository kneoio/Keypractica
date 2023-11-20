package io.kneo.core.repository.exception;

public class UserNotFoundException extends Exception {
    private String  userLogin;

    public UserNotFoundException(String userLogin) {
        super("\"" + userLogin + "\" not found");
        this.userLogin = userLogin;
    }

    public String getDeveloperMessage() {
        return getMessage();
    }
}
