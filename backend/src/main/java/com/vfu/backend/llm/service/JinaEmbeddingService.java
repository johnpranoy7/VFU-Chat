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
public class JinaEmbeddingService implements IEmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${jina.api.key}")
    private String apiKey;

    @Value("${jina.embedding.url}")
    private String jinaEmbeddingUrl;

    @Value("${jina.embedding.model}")
    private String jinaEmbeddingModel;

    @Override
    public List<Double> embedPolicies(String text) {
        return embed(text, "retrieval.passage");
    }

    @Override
    public List<Double> embedQuestion(String text) {
        return embed(text, "retrieval.query");
    }

    private List<Double> embed(String text, String retrievalType) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", jinaEmbeddingModel,
                "task", retrievalType,
                "dimensions", 1024,
                "input", List.of(text)
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        jinaEmbeddingUrl,
                        request,
                        Map.class
                );

        /*
         * Response shape:
         * {
         *   "data": [
         *     { "embedding": [0.01, 0.02, ...] }
         *   ]
         * }
         */
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> data =
                (List<Map<String, Object>>) responseBody.get("data");

        return (List<Double>) data.get(0).get("embedding");
    }
}
