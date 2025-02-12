package com.duoc.productor_kafka.config;


import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.duoc.productor_kafka.dto.SignosVitalesKafkaDTO;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private static final String KAFKA_SERVERS = "44.207.126.62:9092,44.207.126.62:9093,44.207.126.62:9094";

    @Bean
    public ProducerFactory<String, SignosVitalesKafkaDTO> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVERS);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Garantiza que el mensaje se replica correctamente en todos los brokers
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);  // Reintentos en caso de fallo
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1); // Tiempo de espera antes de enviar
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Tamaño del lote de envío

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, SignosVitalesKafkaDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
