package com.vfu.backend.conversation;

import com.vfu.backend.api.dto.ChatResponse;
import com.vfu.backend.intent.IntentClassifier;
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

    private final IntentClassifier classifier;
    private final PolicyService policyService;
    private final HuggingFaceChatService hfChatService;

    public OrchestratorService(IntentClassifier classifier,
                               PolicyService policyService, HuggingFaceChatService hfChatService) {
        this.classifier = classifier;
        this.policyService = policyService;
        this.hfChatService = hfChatService;
    }

    public ChatResponse handle(SessionContext ctx, String message) {

        // 1️ Retrieve relevant policies
        List<RetrievedPolicy> policies =
                policyService.retrieveRelevantPolicies(
                        message, 3);

        double avgVectorSimilarity =
                policyService.averageSimilarity(policies);

        // 2️ Build prompt
        String policyContext = policies.stream()
                .map(RetrievedPolicy::text)
                .collect(Collectors.joining("\n- "));

        String prompt = buildPrompt(
                message,
                policyContext
        );

        // 3️ Ask LLM
        return hfChatService.ask(prompt, avgVectorSimilarity);

    }

    private String buildPrompt(String userMessage, String policyContext) {

        return """
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
                
                USER QUESTION:
                %s
                """.formatted(policyContext, userMessage);
    }

}
