package io.kneo.core.model.user;

import io.kneo.core.server.EnvConst;

import java.util.List;

public interface IUser {
    Long getId();
    String getUserName();
    default Integer getPageSize(){
        return EnvConst.DEFAULT_PAGE_SIZE;
    }
    String getEmail();
    List<IRole> getActivatedRoles();

    default boolean isActive(){
        return true;
    }
}
