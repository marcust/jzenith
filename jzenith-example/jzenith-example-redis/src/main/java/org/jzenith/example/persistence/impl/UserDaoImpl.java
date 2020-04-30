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
import io.vertx.reactivex.redis.client.RedisConnection;
import lombok.NonNull;
import org.jzenith.example.persistence.UserDao;
import org.jzenith.example.persistence.model.Deleted;
import org.jzenith.example.persistence.model.Updated;
import org.jzenith.example.service.model.User;
import org.jzenith.redis.RedisDao;
import org.jzenith.rest.model.Page;
import org.nustaq.serialization.FSTConfiguration;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.UUID;

public class UserDaoImpl extends RedisDao<User> implements UserDao {

    @Inject
    public UserDaoImpl(FSTConfiguration configuration, RedisConnection client) {
        super(configuration, client, User.class);
    }

    @Override
    public Single<User> save(@NonNull User user) {
        return set(user.getId().toString(), user)
                .andThen(Single.just(user));
    }

    @Override
    public Maybe<User> getById(@NonNull UUID id) {
        return get(id.toString());
    }

    @Override
    public Single<Updated> updateNameById(@NonNull UUID id, @NonNull String name) {
        final User updated = new User(id, name);
        return get(id.toString())
                .map(user -> Updated.YES)
                .switchIfEmpty(Single.just(Updated.NO))
                .flatMap(update -> update == Updated.YES ? save(updated).map(user -> Updated.YES) : Single.just(Updated.NO));
    }

    @Override
    public Single<Page<User>> listUsers(@NonNull Integer offset, @NonNull Integer limit) {
        return Single.zip(
                count(),
                list().skip(offset).take(limit).sorted(Comparator.comparing(User::getName)).toList(),
                (count, values) -> new Page<>(offset, limit, count, values));
    }

    @Override
    public Single<Deleted> deleteById(@NonNull UUID id) {
        return delete(id.toString()).map(count -> count > 0 ? Deleted.YES : Deleted.NO);
    }

}
