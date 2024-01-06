package io.kneo.core.repository.table;

public class TableNameResolver implements ITableResolver{
    public static final String USER_ENTITY_NAME = "user";
    public static final String ROLE_ENTITY_NAME = "role";
    public static final String LANGUAGE_ENTITY_NAME = "lang";
    public static final String MODULE_ENTITY_NAME = "module";
    private static final String USER_TABLE_NAME = "_users";
    private static final String ROLE_TABLE_NAME = "_roles";
    private static final String LANGUAGES_TABLE_NAME = "_langs";
    private static final String MODULES_TABLE_NAME = "_modules";

    public EntityData getEntityNames(String type) {
        return switch (type) {
            case USER_ENTITY_NAME ->new EntityData(USER_TABLE_NAME, null);
            case ROLE_ENTITY_NAME ->new EntityData(ROLE_TABLE_NAME, null);
            case LANGUAGE_ENTITY_NAME -> new EntityData(LANGUAGES_TABLE_NAME, null);
            case MODULE_ENTITY_NAME -> new EntityData(MODULES_TABLE_NAME, null);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public static TableNameResolver create() {
        return new TableNameResolver();
    }

}
