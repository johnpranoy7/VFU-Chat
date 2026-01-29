package com.vfu.backend.conversation;

import com.vfu.backend.api.dto.ChatResponse;
import com.vfu.backend.intent.Intent;
import com.vfu.backend.intent.IntentClassifier;
import com.vfu.backend.retrieval.PolicyService;
import com.vfu.backend.session.SessionContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrchestratorService {

    private final IntentClassifier classifier;
    private final PolicyService policies;

    public OrchestratorService(IntentClassifier classifier,
                               PolicyService policies) {
        this.classifier = classifier;
        this.policies = policies;
    }

    public ChatResponse handle(SessionContext ctx, String message) {

        Intent intent = classifier.classify(message);

        if (intent == Intent.UNSUPPORTED) {
            return new ChatResponse(
                    "I'm not confident about that. Please contact guest support.",
                    "LOW",
                    true
            );
        }

        String answer = switch (intent) {
            case CHECK_IN -> "Check-in is at " + ctx.getReservation().getCheckInTime();

            case CHECK_OUT -> "Check-out is at " + ctx.getReservation().getCheckOutTime();

            case AMENITIES -> String.join(", ", ctx.getProperty().getAmenities());

            case HOUSE_RULES -> policies.getPolicies();

            case LOCATION -> ctx.getProperty().getAddress();

            case LOCAL_ATTRACTIONS -> "Nearby beaches and harbor boardwalk.";

            default -> "Unsupported";
        };

        log.info("Classified intent as {}", intent);

        return new ChatResponse(answer, "HIGH", false);
    }
}
