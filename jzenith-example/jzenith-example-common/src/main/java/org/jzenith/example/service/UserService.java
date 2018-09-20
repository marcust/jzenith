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
package org.jzenith.example.service;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.jzenith.example.service.model.User;
import org.jzenith.rest.model.Page;

import java.util.UUID;

public interface UserService {

    Single<User> createUser(String name);

    Single<User> getById(UUID id);

    Single<User> updateById(UUID id, String name);

    Single<Page<User>> listUsers(Integer offset, Integer limit);

    Completable deleteById(UUID id);
}
