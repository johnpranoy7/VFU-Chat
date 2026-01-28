package com.vfu.backend.intent;

import com.vfu.backend.llm.LlmClient;
import org.springframework.stereotype.Service;

@Service
public class IntentClassifier {

    private final LlmClient llm;

    public IntentClassifier(LlmClient llm) {
        this.llm = llm;
    }

    public Intent classify(String message) {
        String prompt = """
        Classify the intent of this message into one of:
        CHECK_IN, CHECK_OUT, AMENITIES, HOUSE_RULES,
        LOCATION, LOCAL_ATTRACTIONS, UNSUPPORTED

        Message: "%s"
        """.formatted(message);

        return Intent.valueOf(llm.complete(prompt).trim());
    }
}

