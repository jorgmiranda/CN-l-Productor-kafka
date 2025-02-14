package com.duoc.productor_kafka.service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.duoc.productor_kafka.auth.OAuth2TokenService;
import com.duoc.productor_kafka.dto.PacienteDTO;
import com.duoc.productor_kafka.dto.SignosVitalesKafkaDTO;
import com.duoc.productor_kafka.dto.SignosVitalesRequestDTO;
import com.duoc.productor_kafka.kafka.SignosVitalesProducer;
import com.duoc.productor_kafka.utils.SignosVitalesUtils;

@Service
public class SignosVitalesScheduler {
    private final SignosVitalesProducer signosVitalesProducer;
    private final RestTemplate restTemplate;
    private final OAuth2TokenService tokenService;
    
    private final String pacientesUrl = "https://9ui00w75xh.execute-api.us-east-1.amazonaws.com/pacientes";
    private final String signosVitalesUrl = "https://9ui00w75xh.execute-api.us-east-1.amazonaws.com/signos-vitales";

    public SignosVitalesScheduler(SignosVitalesProducer signosVitalesProducer, RestTemplate restTemplate, OAuth2TokenService tokenService) {
        this.signosVitalesProducer = signosVitalesProducer;
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
    }

    @Scheduled(fixedRate = 30000) // Ejecuta cada 30 segundos
    public void generarYEnviarSignosVitales() {
        try {
            //Obtener la lista de pacientes con autenticación OAuth2
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + tokenService.getAccessToken());
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<PacienteDTO[]> response = restTemplate.exchange(
                pacientesUrl, HttpMethod.GET, entity, PacienteDTO[].class);

            List<PacienteDTO> pacientes = Arrays.asList(response.getBody());

            if (pacientes.isEmpty()) {
                System.out.println("No hay pacientes disponibles.");
                return;
            }else{
                System.out.println("Pacientes recuperados!");
            }

            //Seleccionar un paciente aleatorio
            Random random = new Random();
            PacienteDTO pacienteSeleccionado = pacientes.get(random.nextInt(pacientes.size()));

            //Generar signos vitales aleatorios
            SignosVitalesRequestDTO signosVitales = new SignosVitalesRequestDTO();
            signosVitales.setPacienteId(pacienteSeleccionado.getId());
            signosVitales.setFrecuenciaCardiaca(SignosVitalesUtils.roundToTwoDecimals(60 + random.nextDouble() * 80));
            signosVitales.setFrecuenciaRespiratoria(SignosVitalesUtils.roundToTwoDecimals(12 + random.nextDouble() * 8));
            signosVitales.setPresionSistolica(SignosVitalesUtils.roundToTwoDecimals(90 + random.nextDouble() * 30));
            signosVitales.setPresionDiastolica(SignosVitalesUtils.roundToTwoDecimals(60 + random.nextDouble() * 20));
            signosVitales.setTemperatura(SignosVitalesUtils.roundToTwoDecimals(36 + random.nextDouble() * 2));
            signosVitales.setSaturacionOxigeno(SignosVitalesUtils.roundToTwoDecimals(90 + random.nextDouble() * 10));

            //Enviar signos vitales con autenticación OAuth2
            HttpEntity<SignosVitalesRequestDTO> request = new HttpEntity<>(signosVitales, headers);
            ResponseEntity<SignosVitalesKafkaDTO> responseSignos = restTemplate.exchange(
                signosVitalesUrl, HttpMethod.POST, request, SignosVitalesKafkaDTO.class);

            if (responseSignos.getBody() != null) {
                //Publicar en Kafka
                signosVitalesProducer.enviarSignosVitales(responseSignos.getBody());
            }

        } catch (Exception e) {
            System.err.println("Error en la comunicación con el backend: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
