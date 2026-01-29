package com.vfu.backend.session;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStore {

    private final Map<String, SessionContext> sessions =
            new ConcurrentHashMap<>();

    public void save(SessionContext ctx) {
        sessions.put(ctx.getSessionId(), ctx);
        MDC.put("sessionId", ctx.getSessionId());
    }

    public Optional<SessionContext> get(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}

