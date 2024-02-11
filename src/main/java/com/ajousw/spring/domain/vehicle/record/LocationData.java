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

    public LocationData(double latitude, double longitude, double direction, LocalDateTime timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.direction = direction;
        this.timestamp = timestamp;
        this.locationName = "";
    }

    public LocationData(double latitude, double longitude, double direction, LocalDateTime timestamp,
                        String locationName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.direction = direction;
        this.timestamp = timestamp;
        this.locationName = locationName;
    }
}

