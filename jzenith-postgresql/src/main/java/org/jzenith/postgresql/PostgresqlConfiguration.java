package org.jzenith.postgresql;

import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.PgPoolOptions;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PostgresqlConfiguration {

    @Builder.Default
    private int port = PgConnectOptions.DEFAULT_PORT;

    @Builder.Default
    private String host = PgConnectOptions.DEFAULT_HOST;

    @Builder.Default
    private String database = PgConnectOptions.DEFAULT_DATABASE;

    @Builder.Default
    private String username = PgConnectOptions.DEFAULT_USER;

    @Builder.Default
    private String password = PgConnectOptions.DEFAULT_PASSWORD;

    @Builder.Default
    private int poolSize = PgPoolOptions.DEFAULT_MAX_POOL_SIZE;


}
