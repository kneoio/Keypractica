package io.kneo.core.model.user;


public class SuperUser extends SystemAbstractUser {
    public final static String USER_NAME = "supervisor";
    public final static long ID = 1;

    public Long getId() {
        return ID;
    }

    @Override
    public String getUserName() {
        return USER_NAME;
    }

    public static SuperUser build() {
        return new SuperUser();
    }

}
