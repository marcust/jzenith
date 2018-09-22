/**
 * Copyright © 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.jdbc;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.opentracing.noop.NoopTracerFactory;
import org.jzenith.core.JZenith;
import org.testcontainers.containers.MySQLContainer;

import java.sql.SQLException;

public abstract class AbstractJdbcPluginTest {

    public static MySQLContainer container = new MySQLContainer();

    static {
        container.start();
    }

    JZenith makeApplication() throws SQLException {
        final MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setPort(container.getFirstMappedPort());
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName(container.getDatabaseName());
        dataSource.setUser(container.getUsername());
        dataSource.setPassword(container.getPassword());
        dataSource.setUseSSL(false);
        dataSource.setAllowPublicKeyRetrieval(true);

        final JZenith application = JZenith.application();
        return application
                .withTracer(NoopTracerFactory.create())
                .withPlugins(JdbcPlugin.create(dataSource, JdbcDatabaseType.MYSQL));
    }



}
