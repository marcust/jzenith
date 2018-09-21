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
package org.jzenith.example;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.jzenith.core.JZenith;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.DriverManager;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class UserResourceIT extends AbstractUserResourceIT {

    private static JZenith jZenith;

    @BeforeClass
    public static void startup() throws Exception {
        jZenith = PostgresJdbcExampleApp.configureApplication();
        injector = jZenith.createInjectorForTesting();
        jZenith.run();
    }

    @AfterClass
    public static void shutdown() {
        if (jZenith != null) {
            jZenith.stop();
        }
    }

    @Override
    protected IDatabaseConnection getConnection() throws Exception {
        // database connection
        PGSimpleDataSource.class.getName();
        Connection jdbcConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5433/test", "test", "test");
        final DatabaseConnection databaseConnection = new DatabaseConnection(jdbcConnection);
        final DatabaseConfig config = databaseConnection.getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());

        return databaseConnection;
    }

}