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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.kafka.client.consumer.OffsetAndMetadata;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DispatcherResult {

    private final Map<TopicPartition, OffsetAndMetadata> commitData;
    private final List<Throwable> throwables;
    private final String originalPayload;

    public DispatcherResult(Map<TopicPartition, OffsetAndMetadata> commitData, String originalPayload, List<Throwable> throwables) {
        this.commitData = ImmutableMap.copyOf(commitData);
        this.originalPayload = originalPayload;
        this.throwables = ImmutableList.copyOf(throwables);
    }

    public static DispatcherResult create(KafkaConsumerRecord<String, String> record, List<HandlerResult> handlerResultList) {
        final Map<TopicPartition, OffsetAndMetadata> commitData = toCommitData(record);

        final List<Throwable> throwables = handlerResultList.stream()
                .filter(HandlerResult::hasThrowable)
                .map(HandlerResult::getThrowable)
                .collect(ImmutableList.toImmutableList());

        return new DispatcherResult(commitData,
                record.value(),
                throwables);
    }

    private static ImmutableMap<TopicPartition, OffsetAndMetadata> toCommitData(KafkaConsumerRecord<String, String> record) {
        return ImmutableMap.of(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset(), null));
    }

    public static DispatcherResult skip(KafkaConsumerRecord<String, String> record) {
        final Map<TopicPartition, OffsetAndMetadata> commitData = toCommitData(record);
        return new DispatcherResult(commitData, record.value(), Collections.emptyList());
    }

    public Map<TopicPartition, OffsetAndMetadata> getCommitData() {
        return commitData;
    }

    public String getOriginalPayload() {
        return originalPayload;
    }

    public boolean isSuccessful() {
        return throwables.isEmpty();
    }
}
