package org.jzenith.postgresql;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PostgresqlConfiguration {

    private int port;
    private String host;
    private String database;

    private String username;
    private String password;

    private int poolSize;


}
