package com.vfu.backend.orchestrator;

import com.vfu.backend.api.dto.ChatResponse;
import com.vfu.backend.llm.IntentType;
import com.vfu.backend.llm.service.IntentClassifierService;
import com.vfu.backend.llm.service.PolicyQaService;
import com.vfu.backend.llm.service.ReservationPropertyEnquiryService;
import org.springframework.stereotype.Service;

@Service
public class HuggingFaceChatOrchestrator {
    private final IntentClassifierService intentClassifierService;
    private final PolicyQaService policyQaService;
    private final ReservationPropertyEnquiryService reservationPropertyEnquiryService;

    public HuggingFaceChatOrchestrator(IntentClassifierService intentClassifierService, PolicyQaService policyQaService, ReservationPropertyEnquiryService reservationPropertyEnquiryService) {
        this.intentClassifierService = intentClassifierService;
        this.policyQaService = policyQaService;
        this.reservationPropertyEnquiryService = reservationPropertyEnquiryService;
    }

    public IntentType classifyIntent(String userMessage) {
        return intentClassifierService.classify(userMessage);
    }

    public ChatResponse answerPolicyQuestionByContext(String knowledgebaseContext, String userMessage, double topEmbedVectorSimilarity) {
        return policyQaService.ask(knowledgebaseContext,userMessage, topEmbedVectorSimilarity);
    }

    public ChatResponse answerReservationPropertyQuestionByContext(String knowledgebaseContext, String userMessage, double topEmbedVectorSimilarity) {
        return reservationPropertyEnquiryService.ask(knowledgebaseContext,userMessage, topEmbedVectorSimilarity);
    }

}
