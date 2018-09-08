package org.jzenith.sql;

import com.google.inject.AbstractModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class SqlBinder extends AbstractModule {

    private final SqlConfiguration configuration;

    public SqlBinder(SqlConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        final HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(configuration.getDataSourceClass().getName());
        config.setUsername(configuration.getUsername());
        config.setPassword(configuration.getPassword());

        bind(DataSource.class).toInstance(new HikariDataSource(config));

    }

}
