package com.vfu.backend.conversation;

import com.vfu.backend.api.dto.ChatResponse;
import com.vfu.backend.llm.service.HuggingFaceChatService;
import com.vfu.backend.model.RetrievedPolicy;
import com.vfu.backend.retrieval.PolicyService;
import com.vfu.backend.session.SessionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrchestratorService {

    private final PolicyService policyService;
    private final HuggingFaceChatService hfChatService;

    public OrchestratorService(PolicyService policyService, HuggingFaceChatService hfChatService) {
        this.policyService = policyService;
        this.hfChatService = hfChatService;
    }

    public ChatResponse handle(SessionContext ctx, String userMessage) {

        // 1️ Retrieve relevant policies
        List<RetrievedPolicy> policies = policyService.retrieveRelevantPolicies(userMessage, 3);
        for(RetrievedPolicy p : policies) {
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
        return hfChatService.ask(policyContext, userMessage, topVectorSimilarity);

    }


}
