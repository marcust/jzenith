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
package org.jzenith.redis;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.vertx.reactivex.redis.RedisClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jzenith.core.JZenith;
import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisDaoTest extends AbstractRedisPluginTest {

    private JZenith application;

    @Data
    @AllArgsConstructor
    static class Entity implements Serializable {
        String aValue;
    }

    static class EntityDao extends RedisDao<Entity> {

        @Inject
        protected EntityDao(FSTConfiguration configuration, RedisClient client) {
            super(configuration, client, Entity.class);
        }
    }

    static class EntityModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EntityDao.class);
        }
    }

    @Inject
    private EntityDao dao;

    @Inject
    private RedisClient client;

    @Inject
    private FSTConfiguration serializer;

    @Before
    public void initClient() {
        application = makeApplication(new EntityModule());
        application.run();
        application.createInjectorForTesting().injectMembers(this);
    }

    @After
    public void closeApplication() {
        if (application != null) {
            application.stop();
        }
    }

    @Test
    public void testStore() {
        dao.set("key", new Entity("value")).blockingGet();

        final Entity entity = client.rxGetBinary(Entity.class.getName() + ":key")
                .map(buffer -> (Entity) serializer.asObject(buffer.getDelegate().getBytes()))
                .blockingGet();

        assertThat(entity.getAValue()).isEqualTo("value");
    }

    @Test
    public void testDelete() {
        testStore();

        final Long deleted = dao.delete("key").blockingGet();

        assertThat(deleted).isEqualTo(1);
    }

    @Test
    public void testList() {
        testStore();

        final List<Entity> entities = dao.list().toList().blockingGet();

        assertThat(entities).hasSize(1);
        assertThat(entities.get(0).getAValue()).isEqualTo("value");
    }

    @Test
    public void testCount() {
        testStore();

        final Long count = dao.count().blockingGet();

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testGet() {
        testStore();

        final Entity entity = dao.get("key").blockingGet();

        assertThat(entity.getAValue()).isEqualTo("value");
    }
}
