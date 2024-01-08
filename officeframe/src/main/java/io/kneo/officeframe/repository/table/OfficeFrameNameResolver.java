package io.kneo.officeframe.repository.table;

import io.kneo.core.repository.table.EntityData;
import io.kneo.core.repository.table.TableNameResolver;

public class OfficeFrameNameResolver extends TableNameResolver {
    public static final String EMPLOYEE = "employee";
    public static final String DEPARTMENT = "department";
    private static final String EMPLOYEE_TABLE_NAME = "staff__employees";
    private static final String DEPARTMENT_TABLE_NAME = "staff__departments";

    public EntityData getEntityNames(String type) {
        return switch (type) {
            case EMPLOYEE -> new EntityData(EMPLOYEE_TABLE_NAME, null);
            case DEPARTMENT -> new EntityData(DEPARTMENT_TABLE_NAME, null);
            default -> super.getEntityNames(type);
        };
    }

    public static OfficeFrameNameResolver create() {
        return new OfficeFrameNameResolver();
    }

}
