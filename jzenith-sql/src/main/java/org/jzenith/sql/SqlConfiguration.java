package org.jzenith.sql;

import lombok.Builder;
import lombok.Getter;

import javax.sql.DataSource;

@Builder
@Getter
public class SqlConfiguration {

    private Class<? extends DataSource> dataSourceClass;
    private String username;
    private String password;

}
