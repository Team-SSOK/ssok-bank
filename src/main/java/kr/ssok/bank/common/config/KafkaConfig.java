package kr.ssok.bank.common.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * 카프카 컨피그 (서버)
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * 요청 수신자 설정
     *
     * @return
     */
    @Bean
    public ConsumerFactory<String, Object> requestConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "request-server-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * 응답 발송자 설정
     *
     * @return
     */
    @Bean
    public ProducerFactory<String, Object> replyProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * 요청-응답 카프카 Producer 템플릿을 생성합니다.
     * @return
     */
    @Bean
    public KafkaTemplate<String, Object> replyTemplate() {
        return new KafkaTemplate<>(replyProducerFactory());
    }

    /**
     * 카프카 리스너 응답 Container 팩토리를 생성합니다.
     *
     * @return
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerReplyContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(requestConsumerFactory());
        factory.setReplyTemplate(replyTemplate());
        // 응답 헤더 설정을 활성화하여 @SendTo가 작동하도록 함
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        // DLQ 설정
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    /**
     * 카프카 리스너 단방향 Container 팩토리를 생성합니다.
     *
     * @return
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerUnidirectionalContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(requestConsumerFactory());
        return factory;
    }

    @Bean
    public CommonErrorHandler errorHandler() {
        // 재시도 정책 설정
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 3L); // 1초 간격으로 3번 재시도
        // DLQ 설정
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                replyTemplate(),
                (consumerRecord, exception) -> {
                    String originalTopic = consumerRecord.topic(); // DLQ 토픽 생성
                    return new TopicPartition(originalTopic + ".dlt", consumerRecord.partition());
                }
        );
        return new DefaultErrorHandler(recoverer, fixedBackOff);
    }

}