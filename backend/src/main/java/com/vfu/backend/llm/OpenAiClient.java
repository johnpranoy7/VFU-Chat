package com.vfu.backend.llm;

import org.springframework.stereotype.Service;

@Service
public class OpenAiClient implements LlmClient {

    @Override
    public String complete(String prompt) {
        // Stubbed for now
        return "CHECK_IN";
    }
}
