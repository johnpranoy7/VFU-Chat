package com.vfu.backend.api.controller;

import com.vfu.backend.api.dto.StartSessionRequest;
import com.vfu.backend.api.dto.StartSessionResponse;
import com.vfu.backend.model.Property;
import com.vfu.backend.model.Reservation;
import com.vfu.backend.retrieval.PropertyService;
import com.vfu.backend.retrieval.ReservationService;
import com.vfu.backend.session.SessionContext;
import com.vfu.backend.session.SessionStore;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/session")
public class SessionController {

    private final SessionStore store;
    private final ReservationService reservations;
    private final PropertyService properties;

    public SessionController(SessionStore store,
                             ReservationService reservations,
                             PropertyService properties) {
        this.store = store;
        this.reservations = reservations;
        this.properties = properties;
    }

    @PostMapping("/")
    public StartSessionResponse start(@Valid @RequestBody StartSessionRequest req) {

        Reservation r = reservations.getReservation(
                req.reservationNumber(),
                req.lastName()
        );

        Property p = properties.getProperty(r.getUnitId());

        String sessionId = UUID.randomUUID().toString();
        store.save(new SessionContext(sessionId, r, p));

        log.info("Starting session for reservation {}", req.reservationNumber());

        return new StartSessionResponse(sessionId);
    }
}

