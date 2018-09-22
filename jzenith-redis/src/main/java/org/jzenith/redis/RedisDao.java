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

import com.google.inject.Provider;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.redis.RedisClient;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.nustaq.serialization.FSTConfiguration;

import java.util.Optional;

public abstract class RedisDao<T> {

    private final FSTConfiguration configuration;

    private final RedisClient client;
    private final Class<T> type;

    protected RedisDao(FSTConfiguration configuration, RedisClient client, Class<T> type) {
        this.configuration = configuration;
        this.client = client;
        this.type = type;
    }

    protected Completable set(@NonNull final String key, @NonNull T value) {
        final Buffer buffer = serialize(value);

        return client.rxSetBinary(prefixKey(key), buffer);
    }

    protected Maybe<T> get(@NonNull final String key) {
        return client.rxGetBinary(prefixKey(key))
                .map(this::deserialize)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    protected Single<Long> delete(@NonNull final String key) {
        return client.rxDel(prefixKey(key));
    }

    public Observable<T> list() {
        return keys()
                .concatMap(key -> client.rxGetBinary(key).toObservable())
                .map(this::deserialize)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public Single<Long> count() {
        return keys().count();
    }

    private Observable<String> keys() {
        return client.rxKeys(type.getName() + ":*")
                .toObservable()
                .flatMapIterable(jsonArray -> jsonArray)
                .filter(String.class::isInstance)
                .map(String.class::cast);
    }

    private String prefixKey(String key) {
        return type.getName() + ":" + key;
    }

    @SuppressWarnings("unchecked")
    private Optional<T> deserialize(Buffer buffer) {
        if (buffer == null) {
            return Optional.empty();
        }

        final io.vertx.core.buffer.Buffer delegate = buffer.getDelegate();
        final byte[] array;
        if (delegate.getByteBuf().hasArray()) {
            array = delegate.getByteBuf().array();
        } else {
            array = delegate.getBytes();
        }

        if (ArrayUtils.isEmpty(array)) {
            return Optional.empty();
        }

        return Optional.of((T) configuration.asObject(array));
    }


    private Buffer serialize(T value) {
        final byte[] bytes = configuration.asByteArray(value);
        return Buffer.newInstance(io.vertx.core.buffer.Buffer.buffer(bytes));
    }
}
