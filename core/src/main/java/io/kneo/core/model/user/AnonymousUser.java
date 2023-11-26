package io.kneo.core.model.user;


public class AnonymousUser extends SystemAbstractUser {
    public final static String USER_NAME = "anonymous";
    public final static long ID = 0L;

    public Long getId() {
        return ID;
    }

    @Override
    public String getUserName() {
        return USER_NAME;
    }

    public static AnonymousUser build() {
        return new AnonymousUser();
    }


}
