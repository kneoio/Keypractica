package com.semantyca.model.user;

import com.semantyca.model.DataEntity;
import com.semantyca.model.IUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User extends DataEntity<Integer> implements IUser {
    private String login;
    private String pwd;
    private String email;
    private boolean authorized;
    private List<String> roles = new ArrayList();

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public void setLogin(String login) {
        this.login = login;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String getPwd() {
        return pwd;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean isAuthorized() {
        return authorized;
    }

    @Override
    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    @Override
    public String getName() {
        return login;
    }

    public Date getLastPasswordResetDate() {
        return new Date();
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }



}
