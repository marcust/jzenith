package org.jzenith.postgresql;

import io.reactiverse.pgclient.PgConnectOptions;
import org.jzenith.core.configuration.ConfigDefault;

public interface PostgresqlConfiguration {

    @ConfigDefault("5432")
    int getPort();

    @ConfigDefault(PgConnectOptions.DEFAULT_HOST)
    String getHost();

    @ConfigDefault(PgConnectOptions.DEFAULT_DATABASE)
    String getDatabase();

    @ConfigDefault(PgConnectOptions.DEFAULT_USER)
    String getUsername();

    @ConfigDefault(PgConnectOptions.DEFAULT_PASSWORD)
    String getPassword();

    @ConfigDefault("4")
    int getPoolSize();
}
