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
package org.jzenith.postgresql;

import io.reactiverse.reactivex.pgclient.PgRowSet;
import io.reactiverse.reactivex.pgclient.Row;
import io.reactivex.Observable;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jzenith.core.JZenith;
import org.jzenith.core.JZenithException;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostgresqlClientTest extends AbstractPostgresqlPluginTest {

    private JZenith application;

    @Inject
    private PostgresqlClient client;

    @Inject
    private DSLContext dslContext;

    @BeforeEach
    public void initClient() {
        application = makeApplication();
        application.run();
        application.createInjectorForTesting().injectMembers(this);
    }

    @AfterEach
    public void closeApplication() {
        if (application != null) {
            application.stop();
        }
    }

    @Test
    public void testClientExecute() {
        final Query query = dslContext.query("select 1");

        final PgRowSet pgRowSet = client.execute(query).blockingGet();

        assertThat(pgRowSet.size()).isEqualTo(1);
        assertThat(pgRowSet.iterator().hasNext()).isTrue();
        assertThat(pgRowSet.iterator().next().getInteger(0)).isEqualTo(1);

    }

    @Test
    public void testClientRowCount() {
        final Query query = dslContext.query("select 1");

        final Integer count = client.executeForRowCount(query).blockingGet();

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testClientForSingleRow() {
        final Query query = dslContext.query("select 1");

        final Row row = client.executeForSingleRow(query).blockingGet();

        assertThat(row.getInteger(0)).isEqualTo(1);
    }

    @Test
    public void testClientForSingleRowNoRow() {
        final Query query = dslContext.query("select 1 where false");

        final Row row = client.executeForSingleRow(query).blockingGet();

        assertThat(row).isNull();
    }

    @Test
    public void testClientForSingleRowMultipleRows() {
        assertThrows(JZenithException.class, () -> {

            final Query query = dslContext.query("select * from pg_indexes");

            client.executeForSingleRow(query).blockingGet();

        });
    }

    @Test
    public void testStreamOffsetLimit() {
        final Query query = dslContext.selectOne()
                .offset(1)
                .limit(2);

        final Observable<Row> row = client.stream(query, 2);

        final List<Row> rows = row.toList().blockingGet();

        assertThat(rows).isNotNull();
    }

    @Test
    public void testStreamLimit() {
        final Query query = dslContext.selectOne()
                .offset(0)
                .limit(2);

        final Observable<Row> row = client.stream(query, 2);

        final List<Row> rows = row.toList().blockingGet();

        assertThat(rows).isNotNull();
    }

    @Test
    public void testNativeQueryErrorHandling() {
        assertThrows(SQLException.class, () -> {

            final PostgresqlClient clientMock = mock(PostgresqlClient.class);
            when(clientMock.parseNativeQuery(any())).thenCallRealMethod();
            when(clientMock.toNativeQuery(any())).thenThrow(new SQLException("error"));

            clientMock.parseNativeQuery(dslContext.selectOne());
        });
    }
}
