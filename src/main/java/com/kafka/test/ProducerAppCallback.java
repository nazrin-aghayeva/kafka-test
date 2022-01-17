package com.kafka.test;

import java.util.Properties;
import java.util.stream.IntStream;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerAppCallback {


    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(ProducerAppCallback.class);

        String bootstrapProperties = "127.0.0.1:9092";
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapProperties);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        IntStream.range(0, 10).forEach(i -> {
            ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", "Hello" + i);
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info(metadata.topic(), metadata.partition(), metadata.offset());
                } else {
                    logger.error("ERROR", exception);
                }
            });
        });


        producer.flush();
        producer.close();
    }

}
