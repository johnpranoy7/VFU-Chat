package com.vfu.backend.api.dto;

import jakarta.validation.constraints.NotEmpty;

public record ChatRequest(
        @NotEmpty(message = "sessionId cannot be empty")
        String sessionId,
        @NotEmpty(message = "Message cannot be empty")
        String message
) {
}
