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
import com.google.inject.Injector;
import io.reactivex.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import kafka.api.OffsetFetchRequest;
import kafka.api.OffsetFetchResponse;
import kafka.client.ClientUtils;
import kafka.common.OffsetMetadataAndError;
import kafka.common.TopicAndPartition;
import kafka.network.BlockingChannel;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jzenith.core.JZenith;
import org.jzenith.core.JZenithException;
import org.jzenith.core.util.TestUtil;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

@Slf4j
@ExtendWith(VertxExtension.class)
public class KafkaConsumerPluginTest extends AbstractKafkaConsumerPluginTest {

    @Test
    public void testStartupShutdown() {
        final JZenith application = makeApplication("test_startup_shutdown");
        application.run();
        application.stop();
    }

    @Test
    public void testApiIsNonNull() {
        TestUtil.testApiMethodsHaveNonNullParameters(KafkaConsumerPlugin.withTopicHandler("test", new TestTopicHandler()));
    }

    @Test
    public void testMessageConsumed()
            throws InterruptedException, ExecutionException, TimeoutException {
        final VertxTestContext testContext = new VertxTestContext();

        final String topicName = "test_message_consumed";
        final JZenith jZenith = makeApplication(topicName, message -> {
            log.info("got message single");
            testContext.completeNow();
            return Single.just(HandlerResult.messageHandled());
        });
        jZenith.run();

        // Define the record we want to produce
        final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, 0, "key", "value");

        // Create a new producer
        try (final KafkaProducer<String, String> producer =
                     getKafkaTestUtils().getKafkaProducer(StringSerializer.class, StringSerializer.class)) {

            // Produce it & wait for it to complete.
            final Future<RecordMetadata> future = producer.send(producerRecord);
            producer.flush();

            future.get(1, TimeUnit.MINUTES);
        }

        testContext.awaitCompletion(1, TimeUnit.MINUTES);

        final OffsetMetadataAndError offsetMetadataAndError = pullCurrentOffset(topicName, jZenith);

        assertThat(offsetMetadataAndError.offset()).isEqualTo(0);
    }

    @Test
    public void testMessageErrored()
            throws InterruptedException, ExecutionException, TimeoutException {
        final VertxTestContext testContext = new VertxTestContext();

        final String topicName = "test_message_errored";
        final JZenith jZenith = makeApplication(topicName, message -> {
            testContext.completeNow();
            throw new JZenithException("test failed message");
        });
        jZenith.run();

        // Define the record we want to produce
        final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, 0, "key", "value");

        // Create a new producer
        try (final KafkaProducer<String, String> producer =
                     getKafkaTestUtils().getKafkaProducer(StringSerializer.class, StringSerializer.class)) {

            // Produce it & wait for it to complete.
            final Future<RecordMetadata> future = producer.send(producerRecord);
            producer.flush();

            future.get(1, TimeUnit.MINUTES);
        }

        testContext.awaitCompletion(1, TimeUnit.MINUTES);
        try {
            pullCurrentOffset(topicName, jZenith);
            fail("Should have timed out as no offset is committed");
        } catch (RuntimeException e) {
            // expected
        }
    }

    private OffsetMetadataAndError pullCurrentOffset(String topicName, JZenith jZenith) {
        final Injector injectorForTesting = jZenith.createInjectorForTesting();
        final String groupName = injectorForTesting.getInstance(KafkaConsumerConfiguration.class).getGroupName();
        final String zkConnectString = sharedKafkaTestResource.getZookeeperConnectString();
        final ZkUtils utils = new ZkUtils(new ZkClient(zkConnectString, Integer.MAX_VALUE, 10000, new ZkSerializer() {
            @Override
            public byte[] serialize(Object data) throws ZkMarshallingError {
                return data.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }), new ZkConnection(zkConnectString), false);

        final TopicAndPartition topicAndPartition = new TopicAndPartition(topicName, 0);
        final Seq<TopicAndPartition> topicAndPartitionSeq = JavaConverters.collectionAsScalaIterable(ImmutableList.of(topicAndPartition)).toSeq();

        return await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> {
                            final BlockingChannel blockingChannel = ClientUtils.channelToOffsetManager(groupName, utils, 100, 100);

                            blockingChannel.send(new OffsetFetchRequest(groupName, topicAndPartitionSeq, OffsetFetchRequest.CurrentVersion(), 0, OffsetFetchRequest.DefaultClientId()));

                            final OffsetFetchResponse offsetFetchResponse = OffsetFetchResponse.readFrom(blockingChannel.receive().payload());

                            blockingChannel.disconnect();

                            final OffsetMetadataAndError offsetMetadataAndError = offsetFetchResponse.requestInfo().get(topicAndPartition).get();

                            return offsetMetadataAndError;

                        },
                        // uncommitted offset is -1, first commit is 0
                        offsetAndMetadataAndError -> offsetAndMetadataAndError.offset() > -1);
    }


}
