package com.vfu.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Reservation {

    private final String reservationNumber;
    private final String lastName;
    private final String unitId;
    private final String checkInDate;
    private final String checkOutDate;
    private final String checkInTime;
    private final String checkOutTime;
}

