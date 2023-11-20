package io.kneo.core.util;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;


public class DatabaseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger("DatabaseUtil");
    @Inject
    DataSource defaultDataSource;


    public boolean testDefaultDatasource() {
        try {
            return  defaultDataSource.getConnection().isValid(10);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }
}

