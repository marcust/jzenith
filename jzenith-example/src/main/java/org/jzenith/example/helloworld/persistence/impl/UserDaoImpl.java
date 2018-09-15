package org.jzenith.example.helloworld.persistence.impl;

import io.reactiverse.pgclient.impl.ArrayTuple;
import io.reactiverse.reactivex.pgclient.PgPool;
import io.reactiverse.reactivex.pgclient.Tuple;
import io.reactivex.Single;
import lombok.NonNull;
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.jzenith.example.helloworld.persistence.UserDao;
import org.jzenith.example.helloworld.service.model.User;
import org.jzenith.postgresql.PostgresqlClient;

import javax.inject.Inject;

import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import static org.jzenith.example.helloworld.persistence.impl.Users.ID_FIELD;
import static org.jzenith.example.helloworld.persistence.impl.Users.NAME_FIELD;
import static org.jzenith.example.helloworld.persistence.impl.Users.USERS_TABLE;

public class UserDaoImpl implements UserDao {


    private final PostgresqlClient client;
    private final DSLContext dslContext;

    @Inject
    public UserDaoImpl(PostgresqlClient client) {
        this.client = client;
        this.dslContext = DSL.using(SQLDialect.POSTGRES_10);
    }

    @Override
    public Single<User> save(@NonNull User user) {
        final Insert<?> insert = dslContext.insertInto(USERS_TABLE)
                .columns(ID_FIELD,
                        NAME_FIELD)
                .values(user.getId(), user.getName());

        return client.execute(insert)
                .map(result -> user);
    }
}
