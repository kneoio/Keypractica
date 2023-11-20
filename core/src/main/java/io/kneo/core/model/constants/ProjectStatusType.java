package io.kneo.core.model.constants;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Kayra created 21-04-2016
 */
public enum ProjectStatusType {
    UNKNOWN(0), DRAFT(899), @Deprecated PROCESSED(900), COMPLETED(901), @Deprecated PROCESSING(902), ACTIVE(903), MERGED(904), PAUSED(910);

    private int code;

    ProjectStatusType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ProjectStatusType getType(int code) {
        for (ProjectStatusType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static List<ProjectStatusType> getActualValues() {
        return Arrays.stream(ProjectStatusType.values()).filter(type -> {
            try {
                Field field = ProjectStatusType.class.getField(type.name());
                if (field.isAnnotationPresent(Deprecated.class)) {
                    return false;
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return type != ProjectStatusType.UNKNOWN;
        }).collect(toList());
    }
}
