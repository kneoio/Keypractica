package io.kneo.projects.repository.table;

import io.kneo.core.repository.table.EntityData;
import io.kneo.core.repository.table.TableNameResolver;

public class ProjectNameResolver extends TableNameResolver {
    public static final String PROJECT = "project";
    public static final String TASK = "task";
    private static final String PROJECT_TABLE_NAME = "prj__projects";
    private static final String PROJECT_ACCESS_TABLE_NAME = "prj__project_readers";
    private static final String TASK_TABLE_NAME = "prj__tasks";
    private static final String TASK_ACCESS_TABLE_NAME = "prj__task_readers";

    public EntityData getEntityNames(String type) {
        return switch (type) {
            case PROJECT -> new EntityData(PROJECT_TABLE_NAME, PROJECT_ACCESS_TABLE_NAME);
            case TASK -> new EntityData(TASK_TABLE_NAME, TASK_ACCESS_TABLE_NAME);
            default -> super.getEntityNames(type);
        };
    }

    public static ProjectNameResolver create() {
        return new ProjectNameResolver();
    }

}
