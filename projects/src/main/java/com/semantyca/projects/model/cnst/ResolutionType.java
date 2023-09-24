package com.semantyca.projects.model.cnst;

/**
 * 
 * @author Kayra created 07-06-2016
 */
@Deprecated
public enum ResolutionType {
	UNKNOWN(0), ACCEPTED(18), DECLINED(19);

	private int code;

	ResolutionType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static ResolutionType getType(int code) {
		for (ResolutionType type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return UNKNOWN;
	}

}
