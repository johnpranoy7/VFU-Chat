package com.vfu.backend.llm.service;

import java.util.List;

public interface EmbeddingService {
    List<Double> embed(String text);
}
