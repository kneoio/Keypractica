package io.kneo.core.model.user;


import io.kneo.core.model.cnst.SystemRoleType;

import java.util.Collections;
import java.util.List;

public class SuperUser extends SystemAbstractUser {
    public final static String USER_NAME = "supervisor";
    public final static long ID = 1;

    public Long getId() {
        return ID;
    }

    @Override
    public String getUserName() {
        return USER_NAME;
    }
    @Override
    public List<IRole> getActivatedRoles() {
        return  Collections.singletonList(new Role.Builder()
                .setIdentifier(SystemRoleType.SUPERVISOR.getName())
                .build()) ;
    }

    public static SuperUser build() {
        return new SuperUser();
    }

}
