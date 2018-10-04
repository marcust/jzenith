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
package org.jzenith.kafka;

import com.google.inject.Module;
import com.salesforce.kafka.test.KafkaTestUtils;
import com.salesforce.kafka.test.junit5.SharedKafkaTestResource;
import io.opentracing.noop.NoopTracerFactory;
import io.reactivex.Single;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.jzenith.core.JZenith;
import org.jzenith.kafka.consumer.HandlerResult;
import org.jzenith.kafka.consumer.KafkaConsumerPlugin;
import org.jzenith.kafka.consumer.TopicHandler;

public abstract class AbstractKafkaPluginTest {

    @RegisterExtension
    public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource()
            // Start a cluster with 2 brokers.
            .withBrokers(2)
            // Disable topic auto-creation.
            .withBrokerProperty("auto.create.topics.enable", "false");

    JZenith makeApplication(Module... modules) {
        sharedKafkaTestResource.getKafkaTestUtils().createTopic("test", 1, (short) 1);
        final JZenith application = JZenith.application();
        return application
                .withTracer(NoopTracerFactory.create())
                .withPlugins(KafkaConsumerPlugin.withTopicHandler("test", new TestTopicHandler()))
                .withConfiguration("kafka.consumer.bootstrap.servers", sharedKafkaTestResource.getKafkaConnectString())
                .withModules(modules);
    }

    static class TestTopicHandler implements TopicHandler<String> {
        @Override
        public Single<HandlerResult> handleMessage(Single<String> messages) {
            return messages.map(message -> {
                System.out.println(message);
                return HandlerResult.success();
            });
        }
    }

}
