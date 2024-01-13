package io.kneo.core.repository.table;

import lombok.Getter;

@Getter
public class EntityData {
    private final String tableName;
    private final String rlsName;
    private final String labelsName;

    public EntityData(String tableName, String rlsName, String labels) {
        this.tableName = tableName;
        this.rlsName = rlsName;
        this.labelsName = labels;
    }
    public EntityData(String tableName, String rlsName) {
        this(tableName, rlsName, null);
    }

    public EntityData(String tableName) {
        this(tableName, null, null);
    }

}


