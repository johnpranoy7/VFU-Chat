package com.vfu.backend.retrieval;

import com.vfu.backend.model.PolicyVector;
import com.vfu.backend.model.RetrievedPolicy;
import com.vfu.backend.orchestrator.JinaEmbeddingOrchestrator;
import com.vfu.backend.repository.PolicyVectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PolicyService {

    private final JinaEmbeddingOrchestrator jinaEmbeddingOrchestrator;
    private final PolicyVectorRepository repository;

    public PolicyService(JinaEmbeddingOrchestrator jinaEmbeddingOrchestrator, PolicyVectorRepository repository) {
        this.jinaEmbeddingOrchestrator = jinaEmbeddingOrchestrator;
        this.repository = repository;
    }

    /**
     * Load policies ONCE (manual trigger)
     */
    public void preloadPolicies(String policyText) {
        List<String> chunks = chunkPolicies(policyText);

        List<PolicyVector> allVectors = new ArrayList<>();

        int i = 0;
        for (String chunk : chunks) {
            List<Double> embedding = jinaEmbeddingOrchestrator.embedAndLoadPolicies(chunk);

            log.info("Preloading {} policy chunks", chunks.size());
            log.info("Embedding Size {}", embedding.size());

            PolicyVector vector = new PolicyVector();
            vector.setId(String.valueOf(i++));
            vector.setText(chunk);
            vector.setEmbedding(embedding);

            allVectors.add(vector);  // Collect ALL policies
        }

        // Save ALL policies as single JSON array
        repository.save(allVectors);  // Changed: pass List, not single vector
        log.info("Saved {} policy vectors", allVectors.size());
    }

    /**
     * Retrieve top-k relevant policy chunks
     */
    public List<RetrievedPolicy> retrieveRelevantPolicies(String question, int k) {
        List<Double> questionEmbedding = jinaEmbeddingOrchestrator.embedQuestion(question);

        log.info("Embedding Size {}", questionEmbedding.size());

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
        // Split on policy section headers FIRST
        String[] sections = policyText.split("(?=\\n[A-Z ]+(POLICY|RULES|RESPONSIBILITIES)\\n?)");

        return Arrays.stream(sections)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            normA += Math.pow(v1.get(i), 2);
            normB += Math.pow(v2.get(i), 2);
        }
        if (normA == 0 || normB == 0) return 0.0;

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public double topSimilarity(List<RetrievedPolicy> policies) {
        return policies.stream()
                .mapToDouble(RetrievedPolicy::similarity)
                .max()
                .orElse(0.0);
    }

}

