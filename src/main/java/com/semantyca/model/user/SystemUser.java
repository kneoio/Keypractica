package com.semantyca.model.user;

import com.semantyca.model.IUser;

import java.util.ArrayList;
import java.util.List;

public abstract class SystemUser implements IUser {
    @Override
    public boolean isAuthorized() {
        return true;
    }

    @Override
    public void setAuthorized(boolean isAuthorized) {
    }

    @Override
    public abstract String getLogin();

    @Override
    public void setLogin(String string) {

    }
    @Override
    public List<String> getRoles() {
        return new ArrayList<>();
    }

    @Override
    public String getPwd() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public void setEmail(String value) {

    }

    @Override
    public abstract String getName();


}
