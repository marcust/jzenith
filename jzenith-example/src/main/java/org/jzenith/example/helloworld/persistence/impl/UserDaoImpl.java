/**
 * Copyright (C) 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.example.helloworld.persistence.impl;

import io.reactiverse.reactivex.pgclient.Row;
import io.reactivex.Maybe;
import io.reactivex.Single;
import lombok.NonNull;
import org.jooq.*;
import org.jzenith.example.helloworld.persistence.UserDao;
import org.jzenith.example.helloworld.persistence.model.Deleted;
import org.jzenith.example.helloworld.persistence.model.Updated;
import org.jzenith.example.helloworld.service.model.User;
import org.jzenith.postgresql.PostgresqlClient;
import org.jzenith.rest.model.Page;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.jooq.impl.DSL.count;
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
    public Maybe<User> getById(@NonNull UUID id) {
        final Select<?> select = dslContext.select(ID_FIELD, NAME_FIELD)
                .from(USERS_TABLE)
                .where(ID_FIELD.eq(id));

        return client.executeForSingleRow(select)
                .map(this::toUser);
    }

    @Override
    public Single<Updated> updateNameById(@NonNull UUID id, @NonNull String name) {
        final Update<?> update = dslContext.update(USERS_TABLE)
                .set(NAME_FIELD, name)
                .where(ID_FIELD.eq(id));

        return client.executeForRowCount(update)
                .map(count -> count > 0 ? Updated.YES : Updated.NO);
    }

    @Override
    public Single<Page<User>> listUsers(@NonNull Integer offset, @NonNull Integer limit) {
        final Select<?> select = dslContext.select(ID_FIELD, NAME_FIELD)
                .from(USERS_TABLE)
                .orderBy(NAME_FIELD.asc())
                .offset(offset)
                .limit(limit);

        final Select<?> count = dslContext.select(count())
                .from(USERS_TABLE);

        return Single.zip(
                client.executeForSingleRow(count).toSingle(),
                client.stream(select, offset, limit).toList(),
                (countRow, valueRows) -> new Page<>(offset, limit, countRow.getLong(0), mapToUsers(valueRows)));
    }

    @Override
    public Single<Deleted> deleteById(@NonNull UUID id) {
        final Delete<?> delete = dslContext.deleteFrom(USERS_TABLE)
                .where(ID_FIELD.eq(id));

        return client.executeForRowCount(delete)
                .map(count -> count > 0 ? Deleted.YES : Deleted.NO);
    }

    private List<User> mapToUsers(List<Row> valueRows) {
        return valueRows.stream().map(this::toUser).collect(toImmutableList());
    }

    private User toUser(Row row) {
        return new User((UUID) row.getValue(ID_FIELD.getName()),
                row.getString(NAME_FIELD.getName()));
    }
}
