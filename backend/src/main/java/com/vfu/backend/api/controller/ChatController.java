package com.vfu.backend.api.controller;

import com.vfu.backend.api.dto.ChatRequest;
import com.vfu.backend.api.dto.ChatResponse;
import com.vfu.backend.conversation.OrchestratorService;
import com.vfu.backend.session.SessionContext;
import com.vfu.backend.session.SessionStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final SessionStore store;
    private final OrchestratorService orchestrator;

    public ChatController(SessionStore store,
                          OrchestratorService orchestrator) {
        this.store = store;
        this.orchestrator = orchestrator;
    }

    @PostMapping("/")
    public ChatResponse chat(@RequestBody ChatRequest req) {

        SessionContext ctx = store.get(req.sessionId())
                .orElseThrow(() -> new RuntimeException("Session expired"));

        return orchestrator.handle(ctx, req.message());
    }
}

