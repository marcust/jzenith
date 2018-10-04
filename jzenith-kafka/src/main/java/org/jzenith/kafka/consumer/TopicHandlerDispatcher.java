package org.jzenith.kafka.consumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.reactivex.Single;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;

import java.util.List;

public class TopicHandlerDispatcher {

    private final Multimap<String, TopicHandler> topicHandlers;

    public TopicHandlerDispatcher(Multimap<String, TopicHandler<?>> topicHandlers) {
        this.topicHandlers = ImmutableMultimap.copyOf(topicHandlers);
    }

    @SuppressWarnings("unchecked")
    public Single<DispatcherResult> handle(KafkaConsumerRecord<String, String> record) {
        String value = record.value();

        final List<Single<HandlerResult>> results = topicHandlers.get(record.topic())
                .parallelStream()
                .map(handler -> handler.handleMessage(Single.just(value)).onErrorResumeNext(e -> Single.just(HandlerResult.fail(e))))
                .collect(ImmutableList.toImmutableList());

        return Single.concat(results)
                .toList()
                .map(resultList -> DispatcherResult.create(record, resultList));
    }

}
