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
package org.jzenith.example.helloworld.persistence;

import io.reactivex.Maybe;
import io.reactivex.Single;
import org.jzenith.example.helloworld.persistence.model.Deleted;
import org.jzenith.example.helloworld.persistence.model.Updated;
import org.jzenith.example.helloworld.service.model.User;
import org.jzenith.rest.model.Page;

import java.util.UUID;

public interface UserDao {

    Single<User> save(User user);

    Maybe<User> getById(UUID id);

    Single<Updated> updateNameById(UUID id, String name);

    Single<Page<User>> listUsers(Integer offset, Integer limit);

    Single<Deleted> deleteById(UUID id);
}
