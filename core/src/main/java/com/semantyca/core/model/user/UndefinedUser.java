package com.semantyca.core.model.user;

import java.util.concurrent.atomic.AtomicReference;

public class UndefinedUser extends SystemUser {
    public final static String USER_NAME = "undefined";
    public final static long ID = Long.valueOf(-999);
    public static final AtomicReference<String> USER_NAME_ATOMIC = new AtomicReference<>(USER_NAME);


    public String getUserID() {
        return USER_NAME;
    }


    public String getUserName() {
        return USER_NAME;
    }


    public Long getId() {
        return ID;
    }


    public String getLogin() {
        return USER_NAME;
    }

    @Override
    public String getName() {
        return USER_NAME;
    }

}
