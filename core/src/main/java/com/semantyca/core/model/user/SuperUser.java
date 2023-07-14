package com.semantyca.core.model.user;


public class SuperUser extends SystemUser {
    public final static String USER_NAME = "supervisor";
    public final static long ID = -1;

    public Long getId() {
        return ID;
    }

    @Override
    public String getLogin() {
        return USER_NAME;
    }


    @Override
    public String getName() {
        return USER_NAME;
    }


}
