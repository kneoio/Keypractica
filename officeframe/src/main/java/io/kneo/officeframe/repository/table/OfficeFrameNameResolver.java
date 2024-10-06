package io.kneo.officeframe.repository.table;

import io.kneo.core.repository.table.EntityData;
import io.kneo.core.repository.table.TableNameResolver;

public class OfficeFrameNameResolver extends TableNameResolver {
    public static final String ORGANIZATION = "organization";
    public static final String EMPLOYEE = "employee";
    public static final String DEPARTMENT = "department";
    public static final String LABEL = "label";
    public static final String POSITION = "position";
    public static final String TASK_TYPE = "task_type";
    public static final String ORG_CATEGORY = "org_category";

    private static final String ORGANIZATION_TABLE_NAME = "__organizations";
    private static final String EMPLOYEE_TABLE_NAME = "__employees";
    private static final String DEPARTMENT_TABLE_NAME = "__departments";
    private static final String LABEL_TABLE_NAME = "__labels";
    private static final String POSITION_TABLE_NAME = "__positions";
    private static final String TASK_TYPE_TABLE_NAME = "__task_types";
    private static final String ORG_CATEGORY_TABLE_NAME = "__org_categories";

    public EntityData getEntityNames(String type) {
        return switch (type) {
            case ORGANIZATION -> new EntityData(String.join(".", DEFAULT_SCHEMA, ORGANIZATION_TABLE_NAME));
            case EMPLOYEE -> new EntityData(String.join(".", DEFAULT_SCHEMA, EMPLOYEE_TABLE_NAME));
            case DEPARTMENT -> new EntityData(String.join(".", DEFAULT_SCHEMA, DEPARTMENT_TABLE_NAME));
            case LABEL -> new EntityData(String.join(".", DEFAULT_SCHEMA, LABEL_TABLE_NAME));
            case POSITION -> new EntityData(String.join(".", DEFAULT_SCHEMA, POSITION_TABLE_NAME));
            case TASK_TYPE -> new EntityData(String.join(".", DEFAULT_SCHEMA, TASK_TYPE_TABLE_NAME));
            case ORG_CATEGORY -> new EntityData(String.join(".", DEFAULT_SCHEMA, ORG_CATEGORY_TABLE_NAME));
            default -> super.getEntityNames(type);
        };
    }

    public static OfficeFrameNameResolver create() {
        return new OfficeFrameNameResolver();
    }
}
