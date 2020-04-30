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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.redis.client.Command;
import io.vertx.reactivex.redis.client.RedisConnection;
import io.vertx.reactivex.redis.client.Request;
import io.vertx.reactivex.redis.client.Response;
import io.vertx.redis.client.impl.types.MultiType;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.nustaq.serialization.FSTConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public abstract class RedisDao<T> {

    private final FSTConfiguration configuration;

    private final RedisConnection client;
    private final Class<T> type;

    protected RedisDao(FSTConfiguration configuration, RedisConnection client, Class<T> type) {
        this.configuration = configuration;
        this.client = client;
        this.type = type;
    }

    protected Completable set(@NonNull final String key, @NonNull T value) {
        final Buffer buffer = serialize(value);

        final Request request = Request.cmd(Command.SET)
                .arg(prefixKey(key))
                .arg(buffer);
        return client.rxSend(request).ignoreElement();
    }

    protected Maybe<T> get(@NonNull final String key) {

        final Request request = Request.cmd(Command.GET)
                .arg(prefixKey(key));
        return client.rxSend(request)
                .map(Response::toBuffer)
                .map(this::deserialize)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    protected Single<Long> delete(@NonNull final String key) {
        final Request request = Request.cmd(Command.DEL)
                .arg(prefixKey(key));

        return client.rxSend(request)
                .map(Response::toLong)
                .toSingle();
    }

    public Observable<T> list() {
        return keys()
                .concatMap(key -> client.rxSend(Request.cmd(Command.GET).arg(key)).toObservable())
                .map(Response::toBuffer)
                .map(this::deserialize)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public Single<Long> count() {
        return keys().count();
    }

    private Observable<String> keys() {
        final Request request = Request.cmd(Command.KEYS)
                .arg(type.getName() + ":*");

        return client.rxSend(request)
                .map(Response::getDelegate)
                .map(MultiType.class::cast)
                .map(MultiType::iterator)
                .map(ImmutableList::copyOf)
                .toObservable()
                .flatMapIterable(list -> list)
                .map(response -> response.toString(StandardCharsets.UTF_8));
    }

    private String prefixKey(String key) {
        return type.getName() + ":" + key;
    }

    @VisibleForTesting
    Optional<T> deserialize(Buffer buffer) {
        if (buffer == null) {
            return Optional.empty();
        }

        final io.vertx.core.buffer.Buffer delegate = buffer.getDelegate();
        return deserialize(delegate);
    }

    @VisibleForTesting
    @SuppressWarnings("unchecked")
    Optional<T> deserialize(io.vertx.core.buffer.Buffer delegate) {
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
