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

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.jzenith.core.JZenith;
import org.jzenith.example.persistence.UserDao;

import java.sql.Connection;
import java.util.UUID;

public class UserResourceIT extends AbstractUserResourceIT {

    private static JZenith jZenith;

    @BeforeClass
    public static void startup() throws Exception {
        jZenith = ExampleApp.configureApplication();
        AbstractUserResourceIT.injector = jZenith.createInjectorForTesting();
        jZenith.run();
    }

    @AfterClass
    public static void shutdown() {
        if (jZenith != null) {
            jZenith.stop();
        }
    }

    protected IDatabaseConnection getConnection() throws Exception {
        // database connection
        Connection jdbcConnection = ExampleApp.createDataSource().getConnection();
        final DatabaseConnection databaseConnection = new DatabaseConnection(jdbcConnection);
        final DatabaseConfig config = databaseConnection.getConfig();
        //config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());

        return databaseConnection;
    }

}
