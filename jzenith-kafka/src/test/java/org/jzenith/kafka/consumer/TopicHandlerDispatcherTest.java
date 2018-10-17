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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import org.junit.jupiter.api.Test;
import org.jzenith.core.JacksonModule;
import org.jzenith.kafka.model.AbstractMessage;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TopicHandlerDispatcherTest {

    @Test
    public void testNoJson() {
        final TopicHandlerDispatcher topicHandlerDispatcher = makeTopicHandlerDispatcher();

        final KafkaConsumerRecord<String, String> mockRecord = mockRecordWithValue("foo");

        final DispatcherResult dispatcherResult = topicHandlerDispatcher.handle(mockRecord).blockingGet();
        assertThat(dispatcherResult.getOriginalPayload()).isEqualTo("foo");
    }

    @Test
    public void testNoType() {
        final TopicHandlerDispatcher topicHandlerDispatcher = makeTopicHandlerDispatcher();

        final KafkaConsumerRecord<String, String> mockRecord = mockRecordWithValue("{}");

        final DispatcherResult dispatcherResult = topicHandlerDispatcher.handle(mockRecord).blockingGet();
        assertThat(dispatcherResult.getOriginalPayload()).isEqualTo("{}");
    }

    @Test
    public void testEmptyType() {
        final TopicHandlerDispatcher topicHandlerDispatcher = makeTopicHandlerDispatcher();

        final String payload = "{\"type\":\"\"}";
        final KafkaConsumerRecord<String, String> mockRecord = mockRecordWithValue(payload);

        final DispatcherResult dispatcherResult = topicHandlerDispatcher.handle(mockRecord).blockingGet();
        assertThat(dispatcherResult.getOriginalPayload()).isEqualTo(payload);
    }

    @Test
    public void testUnknownType() {
        final TopicHandlerDispatcher topicHandlerDispatcher = makeTopicHandlerDispatcher();

        final String payload = "{\"type\":\"foo\"}";
        final KafkaConsumerRecord<String, String> mockRecord = mockRecordWithValue(payload);

        final DispatcherResult dispatcherResult = topicHandlerDispatcher.handle(mockRecord).blockingGet();
        assertThat(dispatcherResult.getOriginalPayload()).isEqualTo(payload);
    }

    @Test
    public void testFullType() {
        final TopicHandlerDispatcher topicHandlerDispatcher = makeTopicHandlerDispatcher();

        final String payload = "{\"type\":\"" + AbstractMessage.class.getName() + "\"}";
        final KafkaConsumerRecord<String, String> mockRecord = mockRecordWithValue(payload);

        final DispatcherResult dispatcherResult = topicHandlerDispatcher.handle(mockRecord).blockingGet();
        assertThat(dispatcherResult.getOriginalPayload()).isEqualTo(payload);
    }

    @SuppressWarnings("unchecked")
    private KafkaConsumerRecord<String, String> mockRecordWithValue(String value) {
        final KafkaConsumerRecord<String, String> mock = mock(KafkaConsumerRecord.class);
        when(mock.value()).thenReturn(value);
        return mock;
    }

    private TopicHandlerDispatcher makeTopicHandlerDispatcher() {
        final ObjectMapper objectMapper = JacksonModule.createObjectMapper();
        return new TopicHandlerDispatcher(objectMapper, ImmutableMultimap.of());
    }

}
