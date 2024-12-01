package io.kneo.core.repository.table;

import lombok.Getter;

@Getter
public class EntityData {
    private final String tableName;
    private final String rlsName;
    private final String labelsName;
    private final String filesTableName;

    public EntityData(String tableName, String rlsName, String labels, String files) {
        this.tableName = tableName;
        this.rlsName = rlsName;
        this.labelsName = labels;
        this.filesTableName = files;
    }

    public EntityData(String tableName, String rlsName) {
        this(tableName, rlsName, null, null);
    }

    public EntityData(String tableName) {
        this(tableName, null, null, null);
    }

    public EntityData(String tableName, String rlsName, String labels) {
        this(tableName, rlsName, labels, null);
    }
}


