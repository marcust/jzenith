package org.jzenith.kafka.consumer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.vertx.kafka.client.consumer.OffsetAndMetadata;
import io.vertx.reactivex.ContextScheduler;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.jzenith.core.AbstractPlugin;
import org.jzenith.core.util.CompletableFutureObserver;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class KafkaConsumerPlugin extends AbstractPlugin {

    private final Multimap<String, TopicHandler<?>> topicHandlers = HashMultimap.create();

    public <T> KafkaConsumerPlugin(String topic, TopicHandler<T> handler) {
        topicHandlers.put(topic, handler);
    }

    @Override
    protected List<Module> getModules() {
        return ImmutableList.of(new KafkaConsumerModule());
    }

    public static <T> KafkaConsumerPlugin withTopicHandler(@NonNull final String topic, @NonNull final TopicHandler<T> handler) {
        return new KafkaConsumerPlugin(topic, handler);
    }

    public <T> KafkaConsumerPlugin andTopicHandler(@NonNull final String topic, @NonNull final TopicHandler<T> handler) {
        topicHandlers.put(topic, handler);

        return this;
    }

    @Override
    protected CompletableFuture<String> start(@NonNull Injector injector) {
        final KafkaConsumer consumer = injector.getInstance(KafkaConsumer.class);
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
        return consumer.toObservable()
                .flatMapSingle(mapper::handle)
                .flatMapSingle(dispatcherResult -> handleResult(dispatcherResult, consumer))
                .subscribeOn(new ContextScheduler(vertx.getDelegate().createSharedWorkerExecutor("kafka-executor"), false))
                .subscribe();
    }

    private Single<Map<TopicPartition, OffsetAndMetadata>> handleResult(DispatcherResult result, KafkaConsumer<String, String> consumer) {
        if (result.isSuccessful()) {
            return new io.vertx.reactivex.core.impl.AsyncResultSingle<>(handler -> consumer.getDelegate().commit(result.getCommitData(), handler));
        } else {
            log.error("Error handling {}, will stopp consuming", result.getOriginalPayload());
            return consumer.rxUnsubscribe().andThen(Single.just(ImmutableMap.of()));
        }
    }

}
