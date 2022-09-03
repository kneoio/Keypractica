package com.semantyca.server;


import io.agroal.api.AgroalDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Configuration {

    @Produces
    public JdbiPlugin sqlObjectPlugin() {
        return new SqlObjectPlugin();
    }

    @Produces
    @Inject
    Jdbi getDataStorage(AgroalDataSource defaultDataSource) {
        return Jdbi.create(defaultDataSource);
    }

}
