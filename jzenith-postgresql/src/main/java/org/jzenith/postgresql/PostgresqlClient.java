package org.jzenith.postgresql;

import com.google.common.collect.Iterables;
import io.reactiverse.pgclient.impl.ArrayTuple;
import io.reactiverse.reactivex.pgclient.PgPool;
import io.reactiverse.reactivex.pgclient.PgRowSet;
import io.reactiverse.reactivex.pgclient.Tuple;
import io.reactivex.Single;
import org.jooq.Query;
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

    public Single<PgRowSet> execute(Query query) {
        try {
            final List<NativeQuery> nativeQueries = Parser.parseJdbcSql(query.getSQL(), true, true, false, false);
            final NativeQuery nativeQuery = Iterables.getOnlyElement(nativeQueries);

            return pgPool.rxPreparedQuery(nativeQuery.nativeSql, new Tuple(new ArrayTuple(query.getBindValues())));
        } catch (SQLException e) {
            return Single.error(e);
        }

    }




}
