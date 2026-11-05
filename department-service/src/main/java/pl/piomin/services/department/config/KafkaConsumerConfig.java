package pl.piomin.services.department.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "department-service"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ProducerFactory<String, String> dltProducerFactory() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS
        );

        properties.put(
                org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.put(
                org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.put(
                org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG,
                "all"
        );

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, String> dltKafkaTemplate() {
        return new KafkaTemplate<>(dltProducerFactory());
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, String> dltKafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        dltKafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        record.topic() + ".DLT",
                                        record.partition()
                                )
                );

        recoverer.setFailIfSendResultIsError(true);

        return recoverer;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            DeadLetterPublishingRecoverer recoverer) {

        // Retry three times, with a 2-second delay between retries.
        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        recoverer,
                        new FixedBackOff(2000L, 3L)
                );

        // Invalid JSON cannot be fixed by repeating deserialization.
        errorHandler.addNotRetryableExceptions(
                DeserializationException.class
        );

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }
}

