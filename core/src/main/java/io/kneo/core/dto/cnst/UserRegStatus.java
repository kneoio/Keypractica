package io.kneo.core.dto.cnst;

public enum UserRegStatus {
    NO_ACCESS(0,"restricted"),
    BANNED(1,"banned"),
    WAITING_FOR_REG_CODE_CONFIRMATION(2, "wfrcc"),
    REGISTERED(3,"registered"),
    SUSPEND(4,"suspend");
    private int code;
    private String alias;

    UserRegStatus(int code, String alias) {
        this.code = code;
        this.alias = alias;
    }

    public static UserRegStatus getType(int code) {
        for (UserRegStatus type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return NO_ACCESS;
    }
    UserRegStatus(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

}
