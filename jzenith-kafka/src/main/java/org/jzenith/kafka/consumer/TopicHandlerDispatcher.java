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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.reactivex.Single;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jzenith.kafka.model.AbstractMessage;

import java.util.List;

@Slf4j
public class TopicHandlerDispatcher {

    private final Multimap<String, TopicHandler<AbstractMessage>> topicHandlers;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public TopicHandlerDispatcher(ObjectMapper objectMapper, Multimap<String, TopicHandler<AbstractMessage>> topicHandlers) {
        this.topicHandlers = ImmutableMultimap.copyOf(topicHandlers);
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public Single<DispatcherResult> handle(KafkaConsumerRecord<String, String> record) {
        final String jsonText = record.value();
        final JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(jsonText);
        } catch (JsonParseException e) {
            log.warn("Could not parse message, message is skipped:\n{}", jsonText, e);
            return Single.just(DispatcherResult.skip(record));
        }
        final JsonNode node = jsonNode.get("type");
        if (node == null) {
            log.warn("Could not get message type, message is skipped:\n{}");
            return Single.just(DispatcherResult.skip(record));
        }

        final String type = node.asText();
        if (StringUtils.isBlank(type)) {
            log.warn("No type in value, message is skipped:\n{}", jsonText);
            return Single.just(DispatcherResult.skip(record));
        }
        final Class<? extends AbstractMessage> messageClass;
        try {
            messageClass = (Class<? extends AbstractMessage>) Class.forName(type);
        } catch (ClassNotFoundException e) {
            log.warn("Can't load class {}, message will be skipped:\n{}", type, jsonText);
            return Single.just(DispatcherResult.skip(record));
        }

        final AbstractMessage message = objectMapper.readValue(jsonText, messageClass);

        final List<Single<HandlerResult>> results = topicHandlers.get(record.topic())
                .parallelStream()
                .map(handler -> Single.just(message).flatMap(v -> handler.handleMessage(Single.just(v)))
                        .onErrorReturn(HandlerResult::fail))
                .collect(ImmutableList.toImmutableList());

        return Single.concat(results)
                .toList()
                .map(resultList -> DispatcherResult.create(record, resultList));
    }

}
