package io.kneo.officeframe.repository.table;

import io.kneo.core.repository.table.EntityData;
import io.kneo.core.repository.table.TableNameResolver;

public class OfficeFrameNameResolver extends TableNameResolver {
    public static final String ORGANIZATION = "organization";
    public static final String EMPLOYEE = "employee";
    public static final String DEPARTMENT = "department";
    public static final String LABEL = "label";
    public static final String POSITION = "position";
    private static final String ORGANIZATION_TABLE_NAME = "staff__orgs";
    private static final String EMPLOYEE_TABLE_NAME = "staff__employees";
    private static final String DEPARTMENT_TABLE_NAME = "staff__departments";
    private static final String LABEL_TABLE_NAME = "ref__labels";
    private static final String POSITION_TABLE_NAME = "ref__positions";

    public EntityData getEntityNames(String type) {
        return switch (type) {
            case ORGANIZATION -> new EntityData(String.join(".",DEFAULT_SCHEMA, ORGANIZATION_TABLE_NAME), null);
            case EMPLOYEE -> new EntityData(String.join(".",DEFAULT_SCHEMA, EMPLOYEE_TABLE_NAME), null);
            case DEPARTMENT -> new EntityData(String.join(".",DEFAULT_SCHEMA, DEPARTMENT_TABLE_NAME), null);
            case LABEL -> new EntityData(String.join(".",DEFAULT_SCHEMA, LABEL_TABLE_NAME), null);
            case POSITION -> new EntityData(String.join(".",DEFAULT_SCHEMA, POSITION_TABLE_NAME), null);
            default -> super.getEntityNames(type);
        };
    }

    public static OfficeFrameNameResolver create() {
        return new OfficeFrameNameResolver();
    }

}
