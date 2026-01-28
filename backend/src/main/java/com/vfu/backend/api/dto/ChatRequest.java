package com.vfu.backend.api.dto;

public record ChatRequest(
        String sessionId,
        String message
) {}
