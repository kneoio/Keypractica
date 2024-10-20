package io.kneo.core.repository.table;

import lombok.Getter;

@Getter
public class EntityData {
    private final String tableName;
    private final String rlsName;
    private final String labelsName;
    private final String imagesTableName;

    public EntityData(String tableName, String rlsName, String labels, String images) {
        this.tableName = tableName;
        this.rlsName = rlsName;
        this.labelsName = labels;
        this.imagesTableName = images;
    }

    public EntityData(String tableName, String rlsName) {
        this(tableName, rlsName, null, null);
    }

    public EntityData(String tableName) {
        this(tableName, null, null, null);
    }

}


