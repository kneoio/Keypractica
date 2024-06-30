package io.kneo.core.model.user;


public class AiAgentUser extends SystemAbstractUser {
    public final static String USER_NAME = "ai_agent";
    public final static long ID = -5;

    public Long getId() {
        return ID;
    }

    @Override
    public String getUserName() {
        return USER_NAME;
    }

    public static AiAgentUser build() {
        return new AiAgentUser();
    }


}
