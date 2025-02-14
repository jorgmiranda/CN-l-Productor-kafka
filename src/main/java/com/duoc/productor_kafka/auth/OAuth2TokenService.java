package com.duoc.productor_kafka.auth;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class OAuth2TokenService {
    private final RestTemplate restTemplate;
    private String token;
    private long expirationTime = 0; // Tiempo de expiración en milisegundos

    // Cargar valores desde application.properties
    @Value("${azure.oauth2.client-id}")
    private String clientId;

    @Value("${azure.oauth2.client-secret}")
    private String clientSecret;

    @Value("${azure.oauth2.scope}")
    private String scope;

    @Value("${azure.oauth2.token-url}")
    private String tokenUrl;

    public OAuth2TokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAccessToken() {
        if (token == null || isTokenExpired()) {
            token = fetchToken();
        }
        return token;
    }

    private String fetchToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("scope", scope);
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = Objects.requireNonNull(response.getBody());
        this.expirationTime = System.currentTimeMillis() + ((Integer) responseBody.get("expires_in") * 1000L);
        return responseBody.get("access_token").toString();
    }

    private boolean isTokenExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }
}
