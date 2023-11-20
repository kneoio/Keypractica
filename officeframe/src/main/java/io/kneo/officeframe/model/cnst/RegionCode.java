package io.kneo.officeframe.model.cnst;

/**
 * @author Kayra created 28-12-2015
 */

public enum RegionCode {
    UNKNOWN(0), URBAN_AGGLOMERATION(601), REGION(602), FEDERATION(603);

    private int code;

    RegionCode(int code) {
        this.code = code;
    }

    public static RegionCode getType(int code) {
        for (RegionCode type : values()) {
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
