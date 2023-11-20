package io.kneo.core.model.user;

import io.kneo.core.server.EnvConst;

public interface IUser {
    Long getId();
    String getUserName();
    default Integer getPageSize(){
        return EnvConst.DEFAULT_PAGE_SIZE;
    };
    String getEmail();
    default int getConfirmationCode(){
        return 0;
    }
}
