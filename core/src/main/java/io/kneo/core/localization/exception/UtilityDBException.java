package io.kneo.core.localization.exception;


import io.vertx.sqlclient.DatabaseException;
import org.postgresql.util.PSQLException;

import java.sql.SQLException;

public class UtilityDBException extends Exception {
    private static final long serialVersionUID = 1L;
    private UtilityDBExceptionType id = UtilityDBExceptionType.INTERNAL_DATABASE_EXCEPTION;
    private String addInfo;

    public UtilityDBException(SQLException exception) {
        super(exception);
        if (exception.getCause() instanceof DatabaseException) {
            DatabaseException de = (DatabaseException) exception.getCause();
            PSQLException psqlException = (PSQLException) de.getCause();
            String code = psqlException.getSQLState();
            if (code.equals("42S02")) {
                id = UtilityDBExceptionType.NO_TABLE;
                addInfo = psqlException.getMessage();
            } else {
                addInfo = psqlException.getMessage();
            }
        } else {
            addInfo = exception.getMessage();
        }
    }

    public UtilityDBException(UtilityDBExceptionType type, String text) {
        super(type.name() + ", addInfo=" + text);
        id = type;
        addInfo = text;
    }

    public UtilityDBExceptionType getType() {
        return id;
    }

    public String getAddInfo() {
        return addInfo;
    }

}
