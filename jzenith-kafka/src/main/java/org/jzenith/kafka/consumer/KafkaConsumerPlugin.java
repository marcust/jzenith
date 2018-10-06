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
package org.jzenith.kafka.consumer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.kafka.client.consumer.OffsetAndMetadata;
import io.vertx.reactivex.ContextScheduler;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.impl.AsyncResultSingle;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jzenith.core.AbstractPlugin;
import org.jzenith.core.util.CompletableFutureObserver;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class KafkaConsumerPlugin extends AbstractPlugin {

    private final Multimap<String, TopicHandler<Object>> topicHandlers = HashMultimap.create();

    private KafkaConsumerPlugin() {

    }

    @Override
    protected List<Module> getModules() {
        return ImmutableList.of(new KafkaConsumerModule());
    }

    public static KafkaConsumerPlugin withTopicHandler(@NonNull final String topic, @NonNull final TopicHandler<?> handler) {
        return new KafkaConsumerPlugin().andTopicHandler(topic, handler);
    }

    @SuppressWarnings("unchecked")
    public KafkaConsumerPlugin andTopicHandler(@NonNull final String topic, @NonNull final TopicHandler<?> handler) {
        topicHandlers.put(topic, (TopicHandler<Object>) handler);

        return this;
    }

    @Override
    protected CompletableFuture<String> start(@NonNull Injector injector) {
        final KafkaConsumer<String, String> consumer = injector.getInstance(Key.get(new TypeLiteral<KafkaConsumer<String, String>>() {}));
        final Vertx vertx = injector.getInstance(Vertx.class);
        final CompletableFutureObserver observer = new CompletableFutureObserver();

        consumer.rxSubscribe(topicHandlers.keySet())
                .subscribe(observer.observer());

        final TopicHandlerDispatcher mapper = new TopicHandlerDispatcher(topicHandlers);

        return observer
                .thenApply(aVoid -> consumerChain(vertx, mapper, consumer))
                .thenApply(aVoid -> "Done");
    }

    private Disposable consumerChain(Vertx vertx, TopicHandlerDispatcher mapper, KafkaConsumer<String, String> consumer) {
        final ContextScheduler scheduler = new ContextScheduler(vertx.getDelegate().createSharedWorkerExecutor("jzenith-kafka-executor"), false);
        return consumer.toObservable()
                .observeOn(scheduler)
                .flatMapSingle(mapper::handle)
                .flatMapSingle(dispatcherResult -> handleResult(dispatcherResult, consumer))
                .subscribeOn(scheduler)
                .subscribe();
    }

    @SuppressWarnings("unchecked")
    private Single<Map<TopicPartition, OffsetAndMetadata>> handleResult(DispatcherResult result, KafkaConsumer<String, String> consumer) {
        if (result.isSuccessful()) {
            return new AsyncResultSingle<>(handler -> consumer.getDelegate().commit(result.getCommitData(), handler));
        } else {
            log.error("Error handling {}, will stop consuming", result.getOriginalPayload());
            return consumer.rxUnsubscribe().andThen(Single.just(ImmutableMap.of()));
        }
    }

}
