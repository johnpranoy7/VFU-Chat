package com.vfu.backend.logging;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class MdcFilter extends OncePerRequestFilter {

    private static final int MAX_SESSION_LENGTH = 36;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws IOException, ServletException {

        try {
            // Unique per request
            MDC.put("requestId", UUID.randomUUID().toString());

            String sessionId = request.getHeader("X-Session-Id");
            if (sessionId != null) {

                // a) Trim to max length
                if (sessionId.length() > MAX_SESSION_LENGTH) {
                    sessionId = sessionId.substring(0, MAX_SESSION_LENGTH);
                }

                // b) Remove control characters to prevent log injection
                sessionId = sessionId.replaceAll("[\\r\\n\\t]", "_");

                // c) Optional: validate pattern (UUID format)
                if (!sessionId.matches("[0-9a-fA-F\\-]{36}")) {
                    sessionId = "INVALID";
                }

                MDC.put("sessionId", sessionId);
                log.info("MDC session set to {}", sessionId);

            } else {
                MDC.put("sessionId", "INVALID");
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            throw ex;
        } finally {
            // VERY IMPORTANT
            MDC.clear();
        }
    }
}

