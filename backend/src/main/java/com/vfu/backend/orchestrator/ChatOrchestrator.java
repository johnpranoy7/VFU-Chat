package com.vfu.backend.orchestrator;

import com.vfu.backend.api.dto.ChatRequest;
import com.vfu.backend.api.dto.ChatResponse;
import com.vfu.backend.llm.IntentType;
import com.vfu.backend.model.Property;
import com.vfu.backend.model.Reservation;
import com.vfu.backend.model.RetrievedPolicy;
import com.vfu.backend.retrieval.PolicyService;
import com.vfu.backend.session.SessionContext;
import com.vfu.backend.session.SessionStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatOrchestrator {

    private final SessionStore sessionStore;

    private final PolicyService policyService;
    private final HuggingFaceChatOrchestrator huggingFaceChatOrchestrator;

    public ChatOrchestrator(SessionStore sessionStore, PolicyService policyService, HuggingFaceChatOrchestrator huggingFaceChatOrchestrator) {
        this.sessionStore = sessionStore;
        this.policyService = policyService;
        this.huggingFaceChatOrchestrator = huggingFaceChatOrchestrator;
    }

    public ChatResponse handleChat(ChatRequest request) {
        String userMessage = request.message();
        String sessionId = request.sessionId();

        IntentType intent = huggingFaceChatOrchestrator.classifyIntent(userMessage);
        log.info("Detected intent: {}", intent);

        return switch (intent) {

            case POLICY -> handlePolicyResponses(userMessage);

            case RESERVATION -> handleReservationResponses(userMessage, sessionId);

            case PROPERTY -> handlePropertyResponses(userMessage, sessionId);

            default -> new ChatResponse(
                    "I'm not sure I can help with that. Please contact guest support.",
                    0.0,
                    true
            );
        };

    }

    private ChatResponse handlePolicyResponses(String userMessage) {
        // 1️ Retrieve relevant policies
        List<RetrievedPolicy> policies = policyService.retrieveRelevantPolicies(userMessage, 3);
        for (RetrievedPolicy p : policies) {
            log.info("Retrieved Policy - Similarity: {}", p.similarity());
        }

        double topVectorSimilarity = policyService.topSimilarity(policies);
        log.info("topVectorSimilarity: {}", topVectorSimilarity);

        if (topVectorSimilarity < 0.45) {
            return new ChatResponse(
                    "I don’t have enough information to answer that. Please contact customer support.",
                    0.0,
                    true
            );
        }

        // 2️ Build prompt
        String policyContext = policies.stream()
                .map(p -> "- " + p.text())
                .collect(Collectors.joining("\n"));
        log.debug("policyContext: {}", policyContext);

        // 3️ Ask LLM
        return huggingFaceChatOrchestrator.answerPolicyQuestionByContext(policyContext, userMessage, topVectorSimilarity);
    }

    private ChatResponse handleReservationResponses(String userMessage, String sessionId) {
        SessionContext ctx = sessionStore.get(sessionId)
                .orElseThrow(() -> new RuntimeException("Session expired"));
        Reservation reservation = ctx.getReservation();

        String reservationContext = """
                Reservation Details:
                        - Guest Last Name: %s
                        - Check-in: %s
                        - Check-out: %s
                        - UnitId: %s
                """.formatted(
                reservation.getLastName(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getUnitId());

        return huggingFaceChatOrchestrator.answerReservationPropertyQuestionByContext(reservationContext, userMessage, calculateConfidence(reservation));
    }

    private ChatResponse handlePropertyResponses(String userMessage, String sessionId) {
        SessionContext ctx = sessionStore.get(sessionId)
                .orElseThrow(() -> new RuntimeException("Session expired"));
        Property property = ctx.getProperty();

        String reservationContext = """
                Property Details:
                        - City: %s
                        - Amenities: %s
                        - House Rules: %s
                        - Property Name: %s
                """.formatted(
                property.getCity(),
                property.getAmenities(),
                property.getHouseRules(),
                property.getName());

        return huggingFaceChatOrchestrator.answerReservationPropertyQuestionByContext(reservationContext, userMessage, calculateConfidence(property));
    }

    public double calculateConfidence(Reservation reservation) {

        double score = 0.0;

        if (reservation.getReservationNumber() != null) score += 0.25;
        if (reservation.getCheckInDate() != null) score += 0.25;
        if (reservation.getCheckOutDate() != null) score += 0.25;
        if (reservation.getUnitId() != null) score += 0.25;

        return score;
    }

    public double calculateConfidence(Property property) {

        double score = 0.0;

        if (property.getName() != null) score += 0.25;
        if (property.getAmenities() != null) score += 0.25;
        if (property.getHouseRules() != null) score += 0.25;
        if (property.getCity() != null) score += 0.25;

        return score;
    }

}
