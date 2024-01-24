package com.ajousw.spring.socket.handler.message.dto;

import java.sql.Timestamp;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationUpdateDto {
    private Long vehicleId;

    private String jwt;

    private Double longitude;

    private Double latitude;

    private boolean usingNavi;

    private double meterPerSec;

    private double direction;

    private Timestamp timestamp;

    @Override
    public String toString() {
        return "LocationUpdate{" +
                "vehicleId=" + vehicleId +
                ", jwt='" + jwt.substring(0, 10) + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", usingNavi=" + usingNavi +
                ", meterPerSec=" + meterPerSec +
                ", direction=" + direction +
                ", timestamp=" + timestamp +
                '}';
    }
}
