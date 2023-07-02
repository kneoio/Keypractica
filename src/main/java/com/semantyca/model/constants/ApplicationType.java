package com.semantyca.model.constants;

public enum ApplicationType {
    UNKNOWN(0, "unknown", "???"), DICTIONARY(100, "dictionary", "dict");

    private int code;
    private String name;
    private String shortName;

    ApplicationType(int code, String name, String shortName) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
    }

    public static ApplicationType getType(int code) {
        for (ApplicationType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public String getShortName() {
        return shortName;
    }

}
