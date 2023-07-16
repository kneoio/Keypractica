package com.semantyca.core.model.user;

public class UndefinedUser extends SystemAbstractUser {
    public final static String USER_NAME = "undefined";
    public final static long ID = Long.valueOf(-999);

    public String getUserID() {
        return USER_NAME;
    }

    public Long getId() {
        return ID;
    }

    public String getUserName() {
        return USER_NAME;
    }

    public static IUser Build() {
        return new UndefinedUser();
    }

}
