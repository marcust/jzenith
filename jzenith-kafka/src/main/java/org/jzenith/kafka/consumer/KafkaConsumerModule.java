package org.jzenith.kafka.consumer;

import com.google.common.base.Joiner;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import org.jzenith.core.configuration.ConfigurationProvider;

import java.util.HashMap;
import java.util.Map;

public class KafkaConsumerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(KafkaConsumerConfiguration.class).toProvider(new ConfigurationProvider<>(KafkaConsumerConfiguration.class));

        bind(KafkaConsumer.class).toProvider(new KafkaConsumerProvider());

    }

    private class KafkaConsumerProvider implements Provider<KafkaConsumer<String,String>> {

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
