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

import com.google.common.base.CaseFormat;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.mongo.WriteOption;
import io.vertx.reactivex.ext.mongo.MongoClient;
import lombok.NonNull;

import java.util.Objects;
import java.util.function.Function;

public class MongoDbDao<T> {

    public static final String ID_FIELD = "_id";
    private final MongoClient client;
    private final String collection;
    private final Function<T, String> idFunction;
    private final Class<T> type;

    protected MongoDbDao(MongoClient client, Class<T> type, Function<T, String> idFunction) {
        this.client = client;
        this.type = type;
        this.collection = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, type.getSimpleName());
        this.idFunction = idFunction;
    }

    protected Maybe<T> get(@NonNull final String key) {
        return client
                .rxFindOne(collection, new JsonObject().put(ID_FIELD, key), null)
                .filter(Objects::nonNull)
                .map(this::mapToType);
    }

    protected Completable insert(@NonNull final T entity) {
        return client
                .rxInsert(collection, JsonObject.mapFrom(entity).put(ID_FIELD, idFunction.apply(entity)))
                .toCompletable();
    }

    protected Completable update(@NonNull final T entity) {
        return client
                .rxUpdateWithOptions(collection,
                        new JsonObject().put(ID_FIELD, idFunction.apply(entity)),
                        new JsonObject().put("$set", JsonObject.mapFrom(entity)),
                        new UpdateOptions()
                                .setUpsert(false)
                                .setMulti(false)
                                .setWriteOption(WriteOption.ACKNOWLEDGED));
    }

    protected Completable delete(@NonNull final String key) {
        return client
                .rxRemoveOne(collection,
                        new JsonObject().put(ID_FIELD, key));
    }

    protected Single<Long> count() {
        return client
                .rxCount(collection,
                        new JsonObject());
    }

    protected Observable<T> list(@NonNull Integer offset, @NonNull Integer limit) {
        final FindOptions findOptions = new FindOptions()
                .setSkip(offset)
                .setLimit(limit);

        return client
                .findBatchWithOptions(collection,
                        new JsonObject(), findOptions)
                .toFlowable()
                .map(this::mapToType)
                .toObservable();
    }

    T mapToType(JsonObject entry) {
        entry.remove(ID_FIELD);
        return entry.mapTo(type);
    }

}
