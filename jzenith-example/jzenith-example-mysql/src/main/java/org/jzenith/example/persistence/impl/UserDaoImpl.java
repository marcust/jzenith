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
package org.jzenith.example.persistence.impl;

import io.reactivex.Maybe;
import io.reactivex.Single;
import lombok.NonNull;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.Insert;
import org.jooq.Select;
import org.jooq.Update;
import org.jzenith.example.persistence.UserDao;
import org.jzenith.example.persistence.model.Deleted;
import org.jzenith.example.persistence.model.Updated;
import org.jzenith.example.service.model.User;
import org.jzenith.jdbc.JdbcClient;
import org.jzenith.jdbc.model.Row;
import org.jzenith.rest.model.Page;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.jooq.impl.DSL.count;

public class UserDaoImpl implements UserDao {

    private final JdbcClient client;
    private final DSLContext dslContext;

    @Inject
    public UserDaoImpl(JdbcClient client, DSLContext dslContext) {
        this.client = client;
        this.dslContext = dslContext;
    }

    @Override
    public Single<User> save(@NonNull User user) {
        final Insert<?> insert = dslContext.insertInto(Users.USERS_TABLE)
                .columns(Users.ID_FIELD,
                        Users.NAME_FIELD)
                .values(user.getId().toString(), user.getName());

        return client.executeInsert(insert)
                .andThen(Single.just(user));
    }

    @Override
    public Maybe<User> getById(@NonNull UUID id) {
        final Select<?> select = dslContext.select(Users.ID_FIELD, Users.NAME_FIELD)
                .from(Users.USERS_TABLE)
                .where(Users.ID_FIELD.eq(id.toString()));

        return client.executeForSingleRow(select)
                .map(this::toUser);
    }

    @Override
    public Single<Updated> updateNameById(@NonNull UUID id, @NonNull String name) {
        final Update<?> update = dslContext.update(Users.USERS_TABLE)
                .set(Users.NAME_FIELD, name)
                .where(Users.ID_FIELD.eq(id.toString()));

        return client.executeForRowCount(update)
                .map(count -> count > 0 ? Updated.YES : Updated.NO);
    }

    @Override
    public Single<Page<User>> listUsers(@NonNull Integer offset, @NonNull Integer limit) {
        final Select<?> select = dslContext.select(Users.ID_FIELD, Users.NAME_FIELD)
                .from(Users.USERS_TABLE)
                .orderBy(Users.NAME_FIELD.asc())
                .offset(offset)
                .limit(limit);

        final Select<?> count = dslContext.select(count())
                .from(Users.USERS_TABLE);

        return Single.zip(
                client.executeForSingleRow(count).toSingle(),
                client.stream(select).toList(),
                (countRow, valueRows) -> new Page<>(offset, limit, countRow.getOnlyLong(), mapToUsers(valueRows)));
    }

    @Override
    public Single<Deleted> deleteById(@NonNull UUID id) {
        final Delete<?> delete = dslContext.deleteFrom(Users.USERS_TABLE)
                .where(Users.ID_FIELD.eq(id.toString()));

        return client.executeForRowCount(delete)
                .map(count -> count > 0 ? Deleted.YES : Deleted.NO);
    }

    private List<User> mapToUsers(List<Row> valueRows) {
        return valueRows.stream().map(this::toUser).collect(toImmutableList());
    }

    private User toUser(Row row) {
        return new User(row.get(Users.ID_FIELD, UUID::fromString),
                row.get(Users.NAME_FIELD));
    }
}
