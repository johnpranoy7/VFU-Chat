package com.vfu.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Property {

    private final String unitId;
    private final String name;
    private final String address;
    private final String city;
    private final List<String> amenities;
    private final Map<String, String> houseRules;
}
