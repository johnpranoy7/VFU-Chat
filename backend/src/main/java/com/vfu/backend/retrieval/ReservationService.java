package com.vfu.backend.retrieval;

import com.vfu.backend.model.Reservation;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    public Reservation getReservation(String reservationNumber, String lastName) {
        return new Reservation(
                reservationNumber,
                lastName,
                "UNIT-1001",
                "2026-03-12",
                "2026-03-16",
                "4:00 PM",
                "10:00 AM"
        );
    }
}

