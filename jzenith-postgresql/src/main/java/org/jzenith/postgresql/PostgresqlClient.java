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
import io.reactiverse.reactivex.pgclient.*;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.NonNull;
import org.jooq.Query;
import org.jzenith.core.JZenithException;
import org.postgresql.core.NativeQuery;
import org.postgresql.core.Parser;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
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
    NativeQuery parseNativeQuery(@NonNull Query query) {
        try {
            final List<NativeQuery> nativeQueries = toNativeQuery(query);
            return Iterables.getOnlyElement(nativeQueries);
        } catch (SQLException e) {
            throw new JZenithException(e);
        }
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
                                PgStream<Row> stream = pq.createStream(limit, new Tuple(new ArrayTuple(retypeBindValues(query, offset, limit))));
                                return stream.toObservable();
                            })
                            .doAfterTerminate(conn::close))
        );
    }

    /**
     * This is a hack, because jOOQ believes that offsets and limits are integers, whereas postgres and thus reactive-pg-client
     * believes them to be int8 aka Long.
     * <p>
     * I actually think reactive-pg-client should upcast that, but as I don't have a minimal test case now and before
     * somebody asks my why I use jOOQ with reactive-pg-client I wait till I publish this and then raise an issue
     */
    private List<Object> retypeBindValues(@NonNull Query query, @NonNull Integer offset, @NonNull Integer limit) {
        final List<Object> bindValues = new ArrayList<>(query.getBindValues());
        if (offset > 0) {
            final int lastElementIndex = bindValues.size() - 1;
            final Object lastBindValue = bindValues.get(lastElementIndex);
            if (offset.equals(lastBindValue)) {
                bindValues.set(lastElementIndex, (long) offset);
            } else {
                throw new IllegalStateException("Expecting limit to be last value in the bind values, but it is " + lastBindValue);
            }

            if (limit > 0) {
                final int secondLastElementIndex = lastElementIndex - 1;
                final Object secondLastBindValue = bindValues.get(secondLastElementIndex);
                if (limit.equals(secondLastBindValue)) {
                    bindValues.set(secondLastElementIndex, (long) limit);
                } else {
                    throw new IllegalStateException("Expecting offset to be second last bind value, but it is " + secondLastBindValue);
                }
            }
        } else {
            if (limit > 0) {
                final int lastElementIndex = bindValues.size() - 1;
                final Object lastBindValue = bindValues.get(lastElementIndex);
                if (limit.equals(lastBindValue)) {
                    bindValues.set(lastElementIndex, (long) limit);
                } else {
                    throw new IllegalStateException("Expecting limit to be last value in the bind values, but it is " + lastBindValue);
                }
            }
        }
        return bindValues;
    }

    private Tuple bindValuesToTuple(Query query) {
        return new Tuple(new ArrayTuple(query.getBindValues()));
    }
}
