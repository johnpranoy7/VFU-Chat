package com.vfu.backend.session;

import com.vfu.backend.model.Property;
import com.vfu.backend.model.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionContext {

    private final String sessionId;
    private final Reservation reservation;
    private final Property property;

}

