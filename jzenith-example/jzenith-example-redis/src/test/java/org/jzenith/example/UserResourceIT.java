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
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.redis.RedisClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.jzenith.core.JZenith;
import org.jzenith.example.service.model.User;
import org.nustaq.serialization.FSTConfiguration;

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
    private FSTConfiguration serializer;

    @Inject
    private RedisClient client;

    @BeforeClass
    public static void startup() throws Exception {
        jZenith = RedisPluginExampleApp.configureApplication();
        injector = jZenith.createInjectorForTesting();
        jZenith.run();
    }

    @AfterClass
    public static void shutdown() {
        if (jZenith != null) {
            jZenith.stop();
        }
    }

    @Before
    public void initalizeData() throws Exception {
        super.setup();

        TEST_DATA.forEach(user -> client.rxSetBinary(user.getClass().getName() + ":" + user.getId(),
                Buffer.newInstance(io.vertx.core.buffer.Buffer.buffer(serializer.asByteArray(user)))).subscribe());
    }

    @After
    public void clear() {
        client.rxFlushall().subscribe();
    }

}