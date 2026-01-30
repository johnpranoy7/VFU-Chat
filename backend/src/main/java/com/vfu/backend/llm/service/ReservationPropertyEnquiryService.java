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
public class ReservationPropertyEnquiryService {

    private final HuggingFaceClient hfClient;
    private final ObjectMapper objectMapper;

    public ReservationPropertyEnquiryService(HuggingFaceClient hfClient, ObjectMapper objectMapper) {
        this.hfClient = hfClient;
        this.objectMapper = objectMapper;
    }

    public ChatResponse ask(String policyContext,
                            String userMessage,
                            double dataCompleteness) {

        String systemPrompt = """
                You are a virtual assistant for a vacation rental company.
                
                RULES:
                - Answer ONLY using the provided reservation/property context.
                - If required data is missing, say you do not have enough information.
                - If any requests are related to modifying or cancelling reservations, instruct the user to contact support.
                - Do NOT assume or invent values.
                - Be concise and factual.
                
                OUTPUT FORMAT (JSON ONLY):
                {
                  "answer": "<string>",
                  "confidence": <number between 0 and 1>
                }
                
                CONTEXT:
                %s
                """.formatted(policyContext);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        );

        String raw = hfClient.chat(messages, 512, 0.3);

        return parseAndEvaluate(raw, dataCompleteness);
    }

    private ChatResponse parseAndEvaluate(String content,
                                          double dataCompleteness) {

        try {
            String cleaned = content
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);

            String answer = node.get("answer").asText();
            double llmConfidence = node.get("confidence").asDouble();

            boolean inferred = containsInference(answer);

            double inferencePenalty = inferred ? 0.15 : 0.0;

            double finalConfidence =
                    (0.6 * llmConfidence)
                            + (0.4 * dataCompleteness)
                            - inferencePenalty;

            boolean escalate =
                    finalConfidence < 0.65 || containsUncertainty(answer);

            log.info("""
                            Reservation/Property QA:
                            llmConfidence={}
                            dataCompleteness={}
                            inferred={}
                            finalConfidence={}
                            """,
                    llmConfidence,
                    dataCompleteness,
                    inferred,
                    finalConfidence
            );

            return new ChatResponse(answer, finalConfidence, escalate);

        } catch (Exception e) {
            return new ChatResponse(
                    "I'm not completely sure. Please contact support.",
                    0.0,
                    true
            );
        }
    }

    private boolean containsInference(String answer) {
        String lower = answer.toLowerCase();
        return lower.contains("likely")
                || lower.contains("probably")
                || lower.contains("assume")
                || lower.contains("may be");
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
