package com.vfu.backend.orchestrator;

import com.vfu.backend.llm.service.JinaEmbeddingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JinaEmbeddingOrchestrator {
    private final JinaEmbeddingService jinaEmbeddingService;

    public JinaEmbeddingOrchestrator(JinaEmbeddingService jinaEmbeddingService) {
        this.jinaEmbeddingService = jinaEmbeddingService;
    }

    public List<Double> embedAndLoadPolicies(String text) {
        return jinaEmbeddingService.embedPolicies(text);
    }

    public List<Double> embedQuestion(String text) {
        return jinaEmbeddingService.embedQuestion(text);
    }

}
