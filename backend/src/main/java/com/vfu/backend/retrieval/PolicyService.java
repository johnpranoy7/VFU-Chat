package com.vfu.backend.retrieval;

import com.vfu.backend.llm.service.EmbeddingService;
import com.vfu.backend.model.PolicyVector;
import com.vfu.backend.model.RetrievedPolicy;
import com.vfu.backend.repository.PolicyVectorRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class PolicyService {

    private final EmbeddingService embeddingService;
    private final PolicyVectorRepository repository;

    public PolicyService(
            EmbeddingService embeddingService,
            PolicyVectorRepository repository) {
        this.embeddingService = embeddingService;
        this.repository = repository;
    }

    /**
     * Load policies ONCE (manual trigger)
     */
    public void preloadPolicies(String policyText) {
        List<String> chunks = chunkPolicies(policyText);

        int i = 0;
        for (String chunk : chunks) {
            List<Double> embedding = embeddingService.embed(chunk);

            PolicyVector vector = new PolicyVector();
            vector.setId(String.valueOf(i++));
            vector.setText(chunk);
            vector.setEmbedding(embedding);

            repository.save(vector);
        }
    }

    /**
     * Retrieve top-k relevant policy chunks
     */
    public List<RetrievedPolicy> retrieveRelevantPolicies(String question, int k) {
        List<Double> questionEmbedding =
                embeddingService.embed(question);

        return repository.findAll().stream()
                .map(v -> new RetrievedPolicy(
                        v.getText(),
                        cosineSimilarity(questionEmbedding, v.getEmbedding())
                ))
                .sorted((a, b) -> Double.compare(b.similarity(), a.similarity()))
                .limit(k)
                .toList();
    }

    // --- helpers below ---

    private List<String> chunkPolicies(String policyText) {
        return Arrays.stream(policyText.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            normA += Math.pow(v1.get(i), 2);
            normB += Math.pow(v2.get(i), 2);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public double averageSimilarity(List<RetrievedPolicy> policies) {
        return policies.stream()
                .mapToDouble(RetrievedPolicy::similarity)
                .average()
                .orElse(0.0);
    }

}

