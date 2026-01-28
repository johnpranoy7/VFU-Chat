package com.vfu.backend.retrieval;

import com.vfu.backend.model.Property;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PropertyService {

    public Property getProperty(String unitId) {
        return new Property(
                unitId,
                "Ocean Breeze Condo",
                "123 Beach Dr, Destin, FL",
                "Destin",
                List.of("Pool", "WiFi", "Parking"),
                 Map.of("pets", "Not allowed")

        );
    }
}

