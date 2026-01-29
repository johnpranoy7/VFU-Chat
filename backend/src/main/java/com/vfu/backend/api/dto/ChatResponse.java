package com.vfu.backend.api.dto;

public record ChatResponse(
        String answer,
        double confidence,
        boolean escalated
) {}

