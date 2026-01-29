package com.vfu.backend.llm.service;

import java.util.List;

public interface IEmbeddingService {
    List<Double> embedPolicies(String text);
    List<Double> embedQuestion(String text);
}
