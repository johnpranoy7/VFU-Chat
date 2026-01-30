package com.vfu.backend.llm.service;

import com.vfu.backend.llm.IntentType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class IntentClassifierService {

    private final HuggingFaceClient hfClient;

    public IntentClassifierService(HuggingFaceClient hfClient) {
        this.hfClient = hfClient;
    }

    public IntentType classify(String userMessage) {

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system",
                        "content", """
                                You are an intent classification system.
                                Classify the user message into ONE of the following intents:
                                POLICY, RESERVATION, PROPERTY, GENERAL.
                                Return ONLY the intent name, nothing else.
                                """),
                Map.of("role", "user", "content", userMessage)
        );

        String intent = hfClient.chat(messages, 50, 0.0);

        return IntentType.valueOf(intent.trim());
    }
}

