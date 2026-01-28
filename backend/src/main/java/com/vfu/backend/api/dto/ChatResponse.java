package com.vfu.backend.api.dto;

public record ChatResponse(
        String answer,
        String confidence,
        boolean escalated
) {}

