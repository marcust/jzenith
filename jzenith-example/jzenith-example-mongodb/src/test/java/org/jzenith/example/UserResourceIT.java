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
package org.jzenith.example;

import com.google.common.collect.ImmutableSet;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.jzenith.core.JZenith;
import org.jzenith.example.service.model.User;
import org.jzenith.mongodb.MongoDbDao;

import javax.inject.Inject;
import java.util.Set;
import java.util.UUID;

public class UserResourceIT extends AbstractUserResourceIT {

    private static JZenith jZenith;

    private static final Set<User> TEST_DATA = ImmutableSet.of(
            new User(USER_UUID, USER_NAME),
            new User(UUID.fromString("3c48292c-34e1-4dd6-bdd1-7f57aed5e31b"), "second_jzenith_user")
    );

    @Inject
    private MongoClient client;

    @BeforeAll
    public static void startup() throws Exception {
        jZenith = MongodbPluginExampleApp.configureApplication();
        injector = jZenith.createInjectorForTesting();
        jZenith.run();
    }

    @AfterAll
    public static void shutdown() {
        if (jZenith != null) {
            jZenith.stop();
        }
    }

    @BeforeEach
    public void initalizeData() throws Exception {
        super.setup();

        TEST_DATA.forEach(user -> client.rxInsert("user",
                JsonObject.mapFrom(user).put(MongoDbDao.ID_FIELD, user.getId().toString())).blockingGet());
    }

    @AfterEach
    public void clear() {
        client.rxDropCollection("user").blockingGet();
    }

}