package com.vfu.backend.api.dto;

public record StartSessionRequest(
        String reservationNumber,
        String lastName
) {
}


