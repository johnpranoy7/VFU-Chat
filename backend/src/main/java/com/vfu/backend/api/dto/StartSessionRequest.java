package com.vfu.backend.api.dto;

import jakarta.validation.constraints.NotEmpty;

public record StartSessionRequest(
        @NotEmpty(message = "reservationNumber cannot be empty")
        String reservationNumber,
        @NotEmpty(message = "lastName cannot be empty")
        String lastName
) {
}


