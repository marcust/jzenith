package org.jzenith.example.helloworld.persistence.impl;

import io.reactiverse.reactivex.pgclient.Row;
import io.reactivex.Maybe;
import io.reactivex.Single;
import lombok.NonNull;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.Select;
import org.jzenith.example.helloworld.persistence.UserDao;
import org.jzenith.example.helloworld.service.model.User;
import org.jzenith.postgresql.PostgresqlClient;

import javax.inject.Inject;
import java.util.UUID;

import static org.jzenith.example.helloworld.persistence.impl.Users.*;

public class UserDaoImpl implements UserDao {

    private final PostgresqlClient client;
    private final DSLContext dslContext;

    @Inject
    public UserDaoImpl(PostgresqlClient client, DSLContext dslContext) {
        this.client = client;
        this.dslContext = dslContext;
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

    @Override
    public Maybe<User> getById(UUID id) {
        final Select<?> select = dslContext.select(ID_FIELD, NAME_FIELD)
                .from(USERS_TABLE)
                .where(ID_FIELD.eq(id));

        return client.executeForSingleRow(select)
                .map(this::toUser);
    }

    private User toUser(Row row) {
        return new User((UUID) row.getValue(ID_FIELD.getName()),
                row.getString(NAME_FIELD.getName()));
    }
}
