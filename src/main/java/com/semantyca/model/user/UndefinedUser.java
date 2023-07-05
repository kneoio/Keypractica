package com.semantyca.model.user;

public class UndefinedUser extends SystemUser {
    public final static String USER_NAME = "undefined";
    public final static long ID = Long.valueOf(-999);


    public String getUserID() {
        return USER_NAME;
    }


    public String getUserName() {
        return USER_NAME;
    }


    public long getId() {
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
