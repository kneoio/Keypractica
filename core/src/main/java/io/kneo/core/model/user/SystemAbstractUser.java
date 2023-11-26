package io.kneo.core.model.user;

import java.util.List;

public abstract class SystemAbstractUser implements IUser{

    @Override
    public String getEmail() {
        return "";
    }

    @Override
    public Integer getPageSize() {
        return 20;
    }

    @Override
    public List<IRole> getActivatedRoles() {
        return null;
    }

}
