package com.vfu.backend.llm;


import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class HuggingFaceClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    @Value("${hf.embedding.model.url}")
    private String hfEmbeddingModelUrl;
    @Value("${hf.apikey}")
    private String apiKey;

    public HuggingFaceClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<Double> generateEmbedding(String text) throws Exception {
        String requestBody = "{\"inputs\": \"" + text + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(hfEmbeddingModelUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // Deserialize JSON array to List<Double>
        return objectMapper.readValue(response.body(), List.class);
    }

    // Optional: Add method for LLM chat generation later
}
