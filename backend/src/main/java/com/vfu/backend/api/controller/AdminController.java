package com.vfu.backend.api.controller;

import com.vfu.backend.llm.service.EmbeddingService;
import com.vfu.backend.model.RetrievedPolicy;
import com.vfu.backend.retrieval.PolicyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final EmbeddingService embeddingService;
    private final PolicyService policyService;

    public AdminController(EmbeddingService embeddingService, PolicyService policyService) {
        this.embeddingService = embeddingService;
        this.policyService = policyService;
    }

    @GetMapping("/embeddings")
    public int testEmbedding(@RequestParam String text) {
        return embeddingService.embed(text).size();
    }

    @PostMapping("/policies/load")
    public String loadPolicies() {
        String policies = """
            No pets allowed
            No smoking inside the unit
            Quiet hours after 10 PM
            """;

        policyService.preloadPolicies(policies);
        return "Policies loaded into vector DB";
    }

    @GetMapping("/policies/search")
    public List<RetrievedPolicy> search(@RequestParam String q) {
        return policyService.retrieveRelevantPolicies(q, 3);
    }
}
