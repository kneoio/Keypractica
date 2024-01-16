package io.kneo.projects.model.cnst;

import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Kayra created 21-04-2016
 */
@Getter
public enum TaskStatus {
    UNKNOWN(0),
    DRAFT(100),
    WAITING_FOR_START(101),
    ACTIVE(102),
    COMPLETED(103),
    MERGED(104),
    PAUSED(105);

    private final int code;

    TaskStatus(int code) {
        this.code = code;
    }

    public static TaskStatus getType(int code) {
        for (TaskStatus type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static List<TaskStatus> getActualValues() {
        return Arrays.stream(TaskStatus.values()).filter(type -> {
            try {
                Field field = TaskStatus.class.getField(type.name());
                if (field.isAnnotationPresent(Deprecated.class)) {
                    return false;
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return type != TaskStatus.UNKNOWN;
        }).collect(toList());
    }
}
