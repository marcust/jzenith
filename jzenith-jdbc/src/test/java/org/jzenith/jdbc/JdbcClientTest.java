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

import io.reactivex.Observable;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jzenith.core.JZenith;
import org.jzenith.jdbc.model.Row;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JdbcClientTest extends AbstractJdbcPluginTest {

    private JZenith application;

    @Inject
    private JdbcClient client;

    @Inject
    private DSLContext dslContext;

    @Before
    public void initClient() throws SQLException {
        application = makeApplication();
        application.run();
        application.createInjectorForTesting().injectMembers(this);
    }

    @After
    public void closeApplication() {
        if (application != null) {
            application.stop();
        }
    }

    @Test
    public void testClientRowCount() {
        final Query query = dslContext.query("SET @ignore_me = 0");

        final Integer count = client.executeForRowCount(query).blockingGet();

        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testClientForSingleRow() {
        final Query query = dslContext.query("select 1");

        final Row row = client.executeForSingleRow(query).blockingGet();

        assertThat(row.getOnlyLong()).isEqualTo(1);
    }

    @Test
    public void testStreamOffsetLimit() {
        final Query query = dslContext.selectOne()
                .offset(1)
                .limit(2);

        final Observable<Row> row = client.stream(query);

        final List<Row> rows = row.toList().blockingGet();

        assertThat(rows).isNotNull();
    }

    @Test
    public void testDelete() {
        final Query query = dslContext.query("insert into testing (data) values ('test insert')");

        client.executeInsert(query).blockingGet();
    }

}
