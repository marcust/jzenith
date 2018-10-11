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
package org.jzenith.example.service.impl;

import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.NonNull;
import org.jzenith.example.persistence.UserDao;
import org.jzenith.example.persistence.model.Deleted;
import org.jzenith.example.persistence.model.Updated;
import org.jzenith.example.service.UserService;
import org.jzenith.example.service.exception.NoSuchUserThrowable;
import org.jzenith.example.service.model.User;
import org.jzenith.rest.model.Page;

import javax.inject.Inject;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Inject
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Single<User> createUser(@NonNull String name) {
        return userDao.save(new User(UUID.randomUUID(), name));
    }

    @Override
    public Single<User> getById(@NonNull UUID id) {
        return userDao.getById(id)
                .switchIfEmpty(Single.error(new NoSuchUserThrowable(id)));
    }

    @Override
    public Single<User> updateById(@NonNull UUID id, @NonNull String name) {
        return userDao.updateNameById(id, name)
                .filter(Updated::isUpdated)
                .switchIfEmpty(Single.error(new NoSuchUserThrowable(id)))
                .flatMap(updated -> getById(id));
    }

    @Override
    public Single<Page<User>> listUsers(@NonNull Integer offset, @NonNull Integer limit) {
        return userDao.listUsers(offset, limit);
    }

    @Override
    public Completable deleteById(UUID id) {
        return getById(id)
                .flatMap(user -> userDao.deleteById(id))
                .filter(Deleted::isDeleted)
                .switchIfEmpty(Single.error(new NoSuchUserThrowable(id)))
                .toCompletable();
    }
}
