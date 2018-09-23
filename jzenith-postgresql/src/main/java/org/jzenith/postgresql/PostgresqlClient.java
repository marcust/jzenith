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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import io.reactiverse.pgclient.impl.ArrayTuple;
import io.reactiverse.reactivex.pgclient.PgIterator;
import io.reactiverse.reactivex.pgclient.PgPool;
import io.reactiverse.reactivex.pgclient.PgResult;
import io.reactiverse.reactivex.pgclient.PgRowSet;
import io.reactiverse.reactivex.pgclient.PgStream;
import io.reactiverse.reactivex.pgclient.Row;
import io.reactiverse.reactivex.pgclient.Tuple;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.jooq.Query;
import org.jzenith.core.JZenithException;
import org.postgresql.core.NativeQuery;
import org.postgresql.core.Parser;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class PostgresqlClient {

    private final PgPool pgPool;

    @Inject
    public PostgresqlClient(PgPool pgPool) {
        this.pgPool = pgPool;
    }

    public Single<PgRowSet> execute(@NonNull Query query) {
        return Single.just(query)
                .map(this::parseNativeQuery)
                .flatMap(nativeQuery -> pgPool.rxPreparedQuery(nativeQuery.nativeSql, bindValuesToTuple(query)));
    }

    @VisibleForTesting
    @SneakyThrows
    NativeQuery parseNativeQuery(@NonNull Query query) {
        final List<NativeQuery> nativeQueries = toNativeQuery(query);
        return Iterables.getOnlyElement(nativeQueries);
    }

    @VisibleForTesting
    List<NativeQuery> toNativeQuery(@NonNull Query query) throws SQLException {
        return Parser.parseJdbcSql(query.getSQL(), true, true, false, false);
    }

    public Single<Integer> executeForRowCount(@NonNull Query query) {
        return execute(query)
                .map(PgResult::rowCount);
    }

    public Maybe<Row> executeForSingleRow(@NonNull Query query) {
        return execute(query)
                .flatMapMaybe(pgRowSet -> {
                    if (pgRowSet.size() > 1) {
                        return Maybe.error(new JZenithException("Expected one result for query '" + query.getSQL() + "' but got " + pgRowSet.size()));
                    }

                    final PgIterator iterator = pgRowSet.iterator();
                    if (iterator.hasNext()) {
                        return Maybe.just(iterator.next());
                    } else {
                        return Maybe.empty();
                    }
                });
    }

    public Observable<Row> stream(@NonNull Query query, @NonNull Integer offset, @NonNull Integer limit) {
        return Observable.just(query)
                .map(this::parseNativeQuery)
                .flatMap(nativeQuery -> pgPool.rxGetConnection()
                        .flatMapObservable(conn -> conn
                                .rxPrepare(nativeQuery.nativeSql)
                                .flatMapObservable(pq -> {
                                    PgStream<Row> stream = pq.createStream(limit, bindValuesToTuple(query));
                                    return stream.toObservable();
                                })
                                .doAfterTerminate(conn::close))
                );
    }

    private Tuple bindValuesToTuple(Query query) {
        return new Tuple(new ArrayTuple(query.getBindValues()));
    }
}
