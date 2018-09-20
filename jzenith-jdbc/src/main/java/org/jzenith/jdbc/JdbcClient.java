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

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.NonNull;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.davidmoten.rx.jdbc.Database;
import org.davidmoten.rx.jdbc.SelectBuilder;
import org.davidmoten.rx.jdbc.UpdateBuilder;
import org.jooq.Query;
import org.jzenith.jdbc.model.Row;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class JdbcClient {

    private final Database database;

    @Inject
    public JdbcClient(Database database) {
        this.database = database;
    }

    public SelectBuilder prepareSelect(@NonNull Query query) {
            return database.select(query.getSQL())
                    .parameters(query.getBindValues())
                    .fetchSize(5);
    }

    public UpdateBuilder prepareUpdate(@NonNull Query query) {
        return database.update(query.getSQL())
                .parameters(query.getBindValues());
    }

    public UpdateBuilder prepareInsert(@NonNull Query query) {
        return database.update(query.getSQL())
                .parameters(query.getBindValues());
    }

    public Completable executeInsert(@NonNull Query query) {
        return prepareInsert(query).complete();
    }

    public UpdateBuilder prepareDelete(@NonNull Query query) {
        return database.update(query.getSQL())
                .parameters(query.getBindValues());
    }

    public Single<Integer> executeForRowCount(@NonNull Query query) {
        return prepareUpdate(query)
                .counts()
                .first(Integer.valueOf(0));
    }

    public Maybe<Row> executeForSingleRow(@NonNull Query query) {
        return prepareSelect(query)
                .get(this::mapRow)
                .firstElement()
                .map(Row::fromMap);
    }

    private Map<String, Object> mapRow(ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();
        final CaseInsensitiveMap<String, Object> map = new CaseInsensitiveMap<>();
        for (int i = 1; i <= columnCount; i++) {
            map.put(metaData.getColumnName(i),resultSet.getObject(i));
        }
        return map;
    }

    public Observable<Row> stream(@NonNull Query query) {
            return prepareSelect(query)
                    .get(this::mapRow)
                    .map(Row::fromMap)
                    .toObservable();
    }


}
