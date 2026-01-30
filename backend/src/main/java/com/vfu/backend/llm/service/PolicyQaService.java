package com.vfu.backend.llm.service;

import com.vfu.backend.api.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PolicyQaService {

    private final HuggingFaceClient hfClient;
    private final ObjectMapper objectMapper;

    public PolicyQaService(HuggingFaceClient hfClient, ObjectMapper objectMapper) {
        this.hfClient = hfClient;
        this.objectMapper = objectMapper;
    }

    public ChatResponse ask(String policyContext,
                            String userMessage,
                            double topEmbedVectorSimilarity) {

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

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        );

        String raw = hfClient.chat(messages, 512, 0.3);

        return parseAndEvaluate(raw, topEmbedVectorSimilarity);
    }

    private ChatResponse parseAndEvaluate(String content,
                                          double similarity) {

        try {
            String cleaned = content
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);

            String answer = node.get("answer").asText();
            double llmConfidence = node.get("confidence").asDouble();

            double finalConfidence =
                    (0.7 * similarity) + (0.3 * llmConfidence);

            boolean escalate =
                    finalConfidence < 0.65 || containsUncertainty(answer);

            return new ChatResponse(answer, llmConfidence, escalate);

        } catch (Exception e) {
            return new ChatResponse(
                    "I'm not completely sure. Please contact support.",
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
