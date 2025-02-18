package com.duoc.productor_kafka.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.duoc.productor_kafka.dto.SignosVitalesKafkaDTO;

@Service
public class SignosVitalesProducer {
    private final KafkaTemplate<String, SignosVitalesKafkaDTO> kafkaTemplate;

    public SignosVitalesProducer(KafkaTemplate<String, SignosVitalesKafkaDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void enviarSignosVitales(SignosVitalesKafkaDTO signosVitales) {
        kafkaTemplate.send("senales_vitales", signosVitales);
        System.out.println("Enviado a Kafka: " + signosVitales);
    }
}
