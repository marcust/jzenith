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

import com.google.common.base.Joiner;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import org.jzenith.core.configuration.ConfigurationProvider;

import java.util.HashMap;
import java.util.Map;

public class KafkaConsumerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(KafkaConsumerConfiguration.class).toProvider(new ConfigurationProvider<>(KafkaConsumerConfiguration.class));
        bind(new TypeLiteral<KafkaConsumer<String, String>>() {}).toProvider(new KafkaConsumerProvider());

    }

    private static class KafkaConsumerProvider implements Provider<KafkaConsumer<String,String>> {

        @Inject
        private KafkaConsumerConfiguration configuration;

        @Inject
        private Vertx vertx;

        @Override
        public KafkaConsumer<String, String> get() {
            final Map<String, String> config = new HashMap<>();
            config.put("bootstrap.servers", Joiner.on(',').join(configuration.getBootstrapServers()));
            config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            config.put("group.id", configuration.getGroupName());
            config.put("auto.offset.reset", "earliest");
            config.put("enable.auto.commit", "false");

            return KafkaConsumer.create(vertx, config);
        }
    }
}
