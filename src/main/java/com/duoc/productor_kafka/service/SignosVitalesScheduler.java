package com.duoc.productor_kafka.service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.duoc.productor_kafka.dto.PacienteDTO;
import com.duoc.productor_kafka.dto.SignosVitalesKafkaDTO;
import com.duoc.productor_kafka.dto.SignosVitalesRequestDTO;

@Service
public class SignosVitalesScheduler {
    private final SignosVitalesProducer signosVitalesProducer;
    private final RestTemplate restTemplate;
    private final String pacientesUrl = "http://backend-url/api/pacientes";
    private final String signosVitalesUrl = "http://backend-url/api/signos-vitales";

    public SignosVitalesScheduler(SignosVitalesProducer signosVitalesProducer, RestTemplate restTemplate) {
        this.signosVitalesProducer = signosVitalesProducer;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 5000) // Ejecuta cada 5 segundos
    public void generarYEnviarSignosVitales() {
        try {
            // Obtener lista de pacientes desde el backend
            ResponseEntity<PacienteDTO[]> response = restTemplate.getForEntity(pacientesUrl, PacienteDTO[].class);
            List<PacienteDTO> pacientes = Arrays.asList(response.getBody());

            if (pacientes.isEmpty()) {
                System.out.println("⚠ No hay pacientes disponibles.");
                return;
            }

            // Seleccionar un paciente aleatorio
            Random random = new Random();
            PacienteDTO pacienteSeleccionado = pacientes.get(random.nextInt(pacientes.size()));

            // Generar signos vitales aleatorios
            SignosVitalesRequestDTO signosVitales = new SignosVitalesRequestDTO();
            signosVitales.setPacienteId(pacienteSeleccionado.getId());
            signosVitales.setFrecuenciaCardiaca(60 + random.nextDouble() * 80);
            signosVitales.setFrecuenciaRespiratoria(12 + random.nextDouble() * 8);
            signosVitales.setPresionSistolica(90 + random.nextDouble() * 30);
            signosVitales.setPresionDiastolica(60 + random.nextDouble() * 20);
            signosVitales.setTemperatura(36 + random.nextDouble() * 2);
            signosVitales.setSaturacionOxigeno(90 + random.nextDouble() * 10);

            // Enviar al backend y obtener la respuesta
            ResponseEntity<SignosVitalesKafkaDTO> responseSignos = restTemplate.postForEntity(signosVitalesUrl, signosVitales, SignosVitalesKafkaDTO.class);
            if (responseSignos.getBody() != null) {
                // Enviar a Kafka
                signosVitalesProducer.enviarSignosVitales(responseSignos.getBody());
            }

        } catch (Exception e) {
            System.err.println("❌ Error al obtener pacientes o enviar signos vitales: " + e.getMessage());
        }
    }
}
