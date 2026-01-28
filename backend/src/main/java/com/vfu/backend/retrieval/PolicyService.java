package com.vfu.backend.retrieval;

import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    public String getPolicies() {
        return """
                - No pets allowed
                - No smoking inside the unit
                - Quiet hours after 10 PM
                """;
    }
}

