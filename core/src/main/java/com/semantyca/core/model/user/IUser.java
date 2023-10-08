package com.semantyca.core.model.user;

import com.semantyca.core.server.EnvConst;

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
