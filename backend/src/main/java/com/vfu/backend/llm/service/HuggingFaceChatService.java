package com.vfu.backend.llm.service;

import com.vfu.backend.api.dto.ChatResponse;
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

@Slf4j
@Service
public class HuggingFaceChatService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${hf.chat.url}")
    private String hfChatUrl;
    @Value("${hf.chat.model}")
    private String hfModel;
    @Value("${hf.api.key}")
    private String apiKey;

    public HuggingFaceChatService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ChatResponse ask(String policyContext, String userMessage, double topEmbedVectorSimilarity) {

        String systemPrompt = """
                You are a virtual assistant for a vacation rental company.
                
                RULES:
                - Answer ONLY using the provided policy context.
                - If the answer is not clearly stated, say you do not have enough information.
                - Do NOT invent policies.
                - Be concise and factual.
                
                OUTPUT FORMAT (JSON ONLY):
                {
                  "answer": "<string>",
                  "confidence": <number between 0 and 1>
                }
                
                POLICY CONTEXT:
                %s
                """.formatted(policyContext);

        log.debug("systemPrompt: {}", systemPrompt);
        log.info("userMessage: {}", userMessage);

        Map<String, Object> body = Map.of(
                "model", hfModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", 512,
                "temperature", 0.3
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(hfChatUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            return escalationFallback();
        }

        return parseAndEvaluate(response.getBody(), topEmbedVectorSimilarity);

    }

    private ChatResponse parseAndEvaluate(String apiResponse,
                                          double topEmbedVectorSimilarity) {

        try {
            JsonNode root = objectMapper.readTree(apiResponse);
            JsonNode content =
                    root.path("choices")
                            .get(0)
                            .path("message")
                            .path("content");

            String cleaned = content.asText()
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode result = objectMapper.readTree(cleaned);

            String answer = result.get("answer").asText();
            double llmChatModelConfidence = result.get("confidence").asDouble();

            log.info("llmChatModelConfidence:{}, answer: {}",llmChatModelConfidence, answer);

            double finalConfidence =
                    (0.7 * topEmbedVectorSimilarity) + (0.3 * llmChatModelConfidence);

            log.info("finalConfidence:{}", finalConfidence);

            boolean escalate =
                    finalConfidence < 0.65 || containsUncertainty(answer);

            return new ChatResponse(answer, llmChatModelConfidence, escalate);

        } catch (Exception e) {
            return escalationFallback();
        }
    }

    private ChatResponse escalationFallback() {
        return new ChatResponse(
                "I'm not completely sure. Please contact guest support for assistance.",
                0.0,
                true
        );
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