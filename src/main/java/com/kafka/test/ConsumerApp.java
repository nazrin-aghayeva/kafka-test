package com.kafka.test;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerApp {
    public static void main(String[] args) {
        new ConsumerApp().run();
    }

    public ConsumerApp() {

    }

    public void run() {

        Logger logger = LoggerFactory.getLogger(ConsumerApp.class);

        String bootstrapServer = "127.0.0.1:9092";
        String groupId = "fourth-app";
        String topic = "firs_topic";
        var countDownLatch = new CountDownLatch(1);
        logger.info("Creating the consumer thread");
        Runnable consumer = new ConsumerThread(countDownLatch, topic, bootstrapServer, groupId);
        var thread = new Thread(consumer);
        thread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("caught shutdown hook");
            ((ConsumerThread) consumer).shutDown();
        }));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Application got error");
        } finally {
            logger.info("Application is closing");
        }
    }

    public class ConsumerThread implements Runnable {
        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;
        private Logger logger = LoggerFactory.getLogger(ConsumerApp.class);


        public ConsumerThread(CountDownLatch latch, String topic, String bootstrapServer, String groupId) {
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            this.latch = latch;

            consumer = new KafkaConsumer<String, String>(properties);

            consumer.subscribe(List.of());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("key: " + record.key() + " value: " + record.value());
                        logger.info("partition: " + record.partition() + " offset: " + record.offset());
                    }
                }
            } catch (WakeupException ex) {
                logger.info("received shutdown");
            } finally {
                consumer.close();
                latch.countDown();
            }
        }

        public void shutDown() {
            consumer.wakeup();
        }
    }
}
