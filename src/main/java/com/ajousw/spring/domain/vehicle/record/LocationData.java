package com.ajousw.spring.domain.vehicle.record;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LocationData {
    private double latitude;
    private double longitude;
    private double direction;
    private LocalDateTime timestamp;
    private String locationName;
    private Double confidence;

    public LocationData(double latitude, double longitude, double direction, LocalDateTime timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.direction = direction;
        this.timestamp = timestamp;
        this.locationName = "";
        this.confidence = null;

        if (direction < 0 || direction >= 360) {
            throw new IllegalArgumentException("Direction must be in range 0 ~ 360");
        }
    }

    public LocationData(double latitude, double longitude, double direction, LocalDateTime timestamp,
                        String locationName, double confidence) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.direction = direction;
        this.timestamp = timestamp;
        this.locationName = locationName;
        this.confidence = confidence;
    }
}

