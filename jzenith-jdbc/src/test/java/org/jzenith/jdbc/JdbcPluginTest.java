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

import org.junit.Test;
import org.jzenith.core.JZenith;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;

public class JdbcPluginTest extends AbstractJdbcPluginTest {

    @Test
    public void testStartupShutdown() throws SQLException {
        final JZenith application = makeApplication();
        application.run();
        application.stop();
    }

    @Test(expected = NullPointerException.class)
    public void testNullParameters() throws SQLException {
        JdbcPlugin.create(null, JdbcDatabaseType.POSTGRES);
    }

    @Test(expected = NullPointerException.class)
    public void testNullParameters2() throws SQLException {
        JdbcPlugin.create(mock(DataSource.class), null);
    }



}
