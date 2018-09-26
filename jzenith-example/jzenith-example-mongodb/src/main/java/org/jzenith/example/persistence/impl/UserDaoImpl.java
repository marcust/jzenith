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
import io.vertx.reactivex.ext.mongo.MongoClient;
import lombok.NonNull;
import org.jzenith.example.persistence.UserDao;
import org.jzenith.example.persistence.model.Deleted;
import org.jzenith.example.persistence.model.Updated;
import org.jzenith.example.service.model.User;
import org.jzenith.mongodb.MongoDbDao;
import org.jzenith.rest.model.Page;

import javax.inject.Inject;
import java.util.UUID;

public class UserDaoImpl extends MongoDbDao<User> implements UserDao {

    @Inject
    public UserDaoImpl(MongoClient client) {
        super(client, User.class, user -> user.getId().toString());
    }

    @Override
    public Single<User> save(@NonNull User user) {
        return super.insert(user)
                .andThen(Single.just(user));
    }

    @Override
    public Maybe<User> getById(@NonNull UUID id) {
        return get(id.toString());
    }

    @Override
    public Single<Updated> updateNameById(@NonNull UUID id, @NonNull String name) {
        return update(new User(id, name))
                .andThen(Single.just(Updated.YES));
    }

    @Override
    public Single<Page<User>> listUsers(@NonNull Integer offset, @NonNull Integer limit) {
        return Single.zip(
                count(),
                list(offset, limit).toList(),
                (count, values) -> new Page<>(offset, limit, count, values));
    }

    @Override
    public Single<Deleted> deleteById(@NonNull UUID id) {
        return delete(id.toString())
                .andThen(Single.just(Deleted.YES))
                .onErrorResumeNext(Single.just(Deleted.NO));
    }


}
