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

import com.salesforce.kafka.test.KafkaTestUtils;
import com.salesforce.kafka.test.junit5.SharedKafkaTestResource;
import io.opentracing.noop.NoopTracerFactory;
import io.reactivex.Single;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.jzenith.core.JZenith;
import org.jzenith.kafka.model.AbstractMessage;

public abstract class AbstractKafkaConsumerPluginTest {

    @RegisterExtension
    public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource()
            // Start a cluster with 2 brokers.
            .withBrokers(2)
            // Disable topic auto-creation.
            .withBrokerProperty("auto.create.topics.enable", "false");

    static KafkaTestUtils getKafkaTestUtils() {
        return sharedKafkaTestResource.getKafkaTestUtils();
    }

    JZenith makeApplication(final String topicName) {
        return makeApplication(topicName, new TestTopicHandler());
    }

    JZenith makeApplication(final String topicName, final TopicHandler<AbstractMessage> topicHandler) {
        getKafkaTestUtils().createTopic(topicName, 1, (short) 1);
        final JZenith application = JZenith.application();
        return application
                .withTracer(NoopTracerFactory.create())
                .withPlugins(KafkaConsumerPlugin.withTopicHandler(topicName, topicHandler))
                .withConfiguration("kafka.consumer.bootstrap.servers", sharedKafkaTestResource.getKafkaConnectString());
    }

    static class TestTopicHandler implements TopicHandler<AbstractMessage> {
        @Override
        public Single<HandlerResult> handleMessage(Single<AbstractMessage> messages) {
            return messages.map(message -> {
                System.out.println(message);
                return HandlerResult.messageHandled();
            });
        }
    }

    static class TestMessage extends AbstractMessage {

        private String payload;

        public String getPayload() {
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }
    }

}
