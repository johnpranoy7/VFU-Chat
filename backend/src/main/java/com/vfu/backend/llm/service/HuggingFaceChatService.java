package com.vfu.backend.llm.service;

import com.vfu.backend.api.dto.ChatResponse;
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
public class HuggingFaceChatService {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${hf.chat.model.url}")
    private String hfChatUrl;
    @Value("${hf.apikey}")
    private String apiKey;

    public ChatResponse ask(String prompt, double avgVectorSimilarity) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "inputs", prompt,
                "parameters", Map.of(
                        "max_new_tokens", 250,
                        "return_full_text", false
                )
        );

        HttpEntity<?> request = new HttpEntity<>(body, headers);

        ResponseEntity<List> response =
                restTemplate.postForEntity(hfChatUrl, request, List.class);

        Map<?, ?> result = (Map<?, ?>) response.getBody().get(0);
        String rawText = result.get("generated_text").toString();

        return parseAndEvaluate(rawText, avgVectorSimilarity);
    }

    private ChatResponse parseAndEvaluate(String rawJson, double avgVectorSimilarity) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(rawJson);

            String answer = node.get("answer").asText();
            double llmConfidence = node.get("confidence").asDouble();
            // Combined confidence (retrieval + LLM)
            double finalConfidence =
                    (0.7 * avgVectorSimilarity) +
                            (0.3 * llmConfidence);


            boolean escalation =
                    finalConfidence < 0.65 ||
                            containsUncertainty(answer);

            return new ChatResponse(answer, llmConfidence, escalation);

        } catch (Exception e) {
            // If parsing fails → ALWAYS escalate
            return new ChatResponse(
                    "I'm not completely sure. Please contact guest support.",
                    0.0,
                    true
            );
        }
    }

    private boolean containsUncertainty(String answer) {
        String lower = answer.toLowerCase();
        return lower.contains("not sure")
                || lower.contains("cannot find")
                || lower.contains("no information")
                || lower.contains("unsure")
                || lower.contains("contact support");
    }


}
