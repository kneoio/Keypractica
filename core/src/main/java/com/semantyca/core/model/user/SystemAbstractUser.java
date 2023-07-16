package com.semantyca.core.model.user;

import com.semantyca.core.server.EnvConst;

public abstract class SystemAbstractUser implements IUser{
    @Override
    public Integer getPageSize() {
        return EnvConst.DEFAULT_PAGE_SIZE;
    }

    @Override
    public String getEmail() {
        return "";
    }


}
