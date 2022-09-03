package com.semantyca.model;

import java.util.List;


public interface IUser {

    Integer getId();

    String getLogin();

    default String getName(){
        return getLogin();
    }

    void setLogin(String string);

    String getPwd();

    List<String> getRoles();

    boolean isAuthorized();

    void setAuthorized(boolean isAuthorized);

    String getEmail();

    void setEmail(String value);

}
