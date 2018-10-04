package org.jzenith.kafka.consumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.kafka.client.consumer.OffsetAndMetadata;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;

import java.util.List;
import java.util.Map;

public class DispatcherResult {

    private final Map<TopicPartition, OffsetAndMetadata> commitData;
    private final List<Exception> exceptions;
    private final String originalPayload;

    public DispatcherResult(Map<TopicPartition, OffsetAndMetadata> commitData, String originalPayload, List<Exception> exceptions) {
        this.commitData = ImmutableMap.copyOf(commitData);
        this.originalPayload = originalPayload;
        this.exceptions = ImmutableList.copyOf(exceptions);
    }

    public static DispatcherResult create(KafkaConsumerRecord<String, String> record, List<HandlerResult> handlerResultList) {
        final Map<TopicPartition, OffsetAndMetadata> commitData = ImmutableMap.of(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset(), null));

        final List<Exception> exceptions = handlerResultList.stream()
                .filter(HandlerResult::hasException)
                .map(HandlerResult::getThrowable)
                .collect(ImmutableList.toImmutableList());

        return new DispatcherResult(commitData,
                record.value(),
                exceptions);
    }

    public Map<TopicPartition, OffsetAndMetadata> getCommitData() {
        return commitData;
    }

    public String getOriginalPayload() {
        return originalPayload;
    }

    public boolean isSuccessful() {
        return exceptions.isEmpty();
    }
}
