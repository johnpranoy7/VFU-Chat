package com.vfu.backend.llm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HuggingFaceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${hf.chat.url}")
    private String hfChatUrl;

    @Value("${hf.chat.model}")
    private String hfModel;

    @Value("${hf.api.key}")
    private String apiKey;

    public HuggingFaceClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String chat(List<Map<String, String>> messages,
                       int maxTokens,
                       double temperature) {

        Map<String, Object> body = Map.of(
                "model", hfModel,
                "messages", messages,
                "max_tokens", maxTokens,
                "temperature", temperature
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(hfChatUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("HF Chat API failed: " + response.getStatusCode());
        }

        return extractContent(response.getBody());
    }

    private String extractContent(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse HF response", e);
        }
    }
}
