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
package org.jzenith.mongodb;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jzenith.core.JZenith;
import org.jzenith.core.util.TestUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoDbDaoTest extends AbstractMongoDbPluginTest {

    private JZenith application;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Entity implements Serializable {
        String aKey;
        String aValue;
    }

    static class EntityDao extends MongoDbDao<Entity> {

        @Inject
        protected EntityDao(MongoClient client) {
            super(client, Entity.class, Entity::getAKey);
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
    private MongoClient client;

    @BeforeEach
    public void initClient() {
        application = makeApplication(new EntityModule());
        application.run();
        application.createInjectorForTesting().injectMembers(this);
    }

    @AfterEach
    public void closeApplication() {
        client.rxDropCollection("entity").blockingGet();
        if (application != null) {
            application.stop();
        }
    }

    @Test
    public void testInsert() {
        dao.insert(new Entity("key", "value")).blockingGet();

        final Entity entity = client.rxFindOne("entity", new JsonObject().put(MongoDbDao.ID_FIELD, "key"), null)
                .filter(Objects::nonNull)
                .map(dao::mapToType)
                .blockingGet();

        assertThat(entity.getAValue()).isEqualTo("value");
        assertThat(entity.getAKey()).isEqualTo("key");
    }

    @Test
    public void testDelete() {
        testInsert();

        dao.delete("key").blockingGet();

        final Entity entity = client.rxFindOne("entity", new JsonObject().put(MongoDbDao.ID_FIELD, "key"), new JsonObject())
                .filter(Objects::nonNull)
                .map(dao::mapToType)
                .blockingGet();

        assertThat(entity).isNull();
    }

    @Test
    public void testList() {
        testInsert();

        final List<Entity> entities = dao.list(0, 10).toList().blockingGet();

        assertThat(entities).hasSize(1);
        assertThat(entities.get(0).getAValue()).isEqualTo("value");
    }

    @Test
    public void testCount() {
        testInsert();

        final Long count = dao.count().blockingGet();

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testGet() {
        testInsert();

        final Entity entity = dao.get("key").blockingGet();

        assertThat(entity.getAValue()).isEqualTo("value");
    }

    @Test
    public void testUpdate() {
        testInsert();

        dao.update(new Entity("key", "newValue")).blockingGet();

        final Entity entity = client.rxFindOne("entity", new JsonObject().put(MongoDbDao.ID_FIELD, "key"), null)
                .filter(Objects::nonNull)
                .map(dao::mapToType)
                .blockingGet();

        assertThat(entity.getAValue()).isEqualTo("newValue");
    }

    @Test
    public void testPublicMethodsHaveNonNullParameters() {
        TestUtil.testApiMethodsHaveNonNullParameters(dao);
    }

}
