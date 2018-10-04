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
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.reactivex.Single;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;

import java.util.List;

public class TopicHandlerDispatcher {

    private final Multimap<String, TopicHandler<Object>> topicHandlers;

    @SuppressWarnings("unchecked")
    public TopicHandlerDispatcher(Multimap<String, TopicHandler<Object>> topicHandlers) {
        this.topicHandlers = ImmutableMultimap.copyOf(topicHandlers);
    }

    @SuppressWarnings("unchecked")
    public Single<DispatcherResult> handle(KafkaConsumerRecord<String, String> record) {
        String value = record.value();

        final List<Single<HandlerResult>> results = topicHandlers.get(record.topic())
                .parallelStream()
                .map(handler -> handler.handleMessage(Single.just(value))
                        .onErrorReturn(HandlerResult::fail))
                .collect(ImmutableList.toImmutableList());

        return Single.concat(results)
                .toList()
                .map(resultList -> DispatcherResult.create(record, resultList));
    }

}
