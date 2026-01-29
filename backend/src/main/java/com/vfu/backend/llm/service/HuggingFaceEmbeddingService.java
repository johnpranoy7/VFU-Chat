package com.vfu.backend.llm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceEmbeddingService implements EmbeddingService {

    private final RestTemplate restTemplate;

    @Value("${hf.apikey}")
    private String apiKey;

    @Value("${hf.embedding.model.url}")
    private String hfEmbeddingModelUrl;

    public HuggingFaceEmbeddingService() {

        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<Double> embed(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("inputs", text);
        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<List> response =
                restTemplate.postForEntity(
                        hfEmbeddingModelUrl,
                        request,
                        List.class
                );

        return (List<Double>) response.getBody();
    }
}

