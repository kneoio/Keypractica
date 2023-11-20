package io.kneo.core.service.messaging;

/**
 * @author Kayra created 08-09-2016
 */
public enum MessagingType {
    UNKNOWN(0), EMAIL(56), SLACK(57), XMPP(58), SITE(59);

    private int code;

    MessagingType(int code) {
        this.code = code;
    }

    public static MessagingType getType(int code) {
        for (MessagingType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }

}
