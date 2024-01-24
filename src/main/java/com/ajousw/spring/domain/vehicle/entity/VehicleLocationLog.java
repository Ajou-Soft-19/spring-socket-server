package com.ajousw.spring.domain.vehicle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VehicleLocationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vehicleId;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point coordinate;

    private LocalDateTime lastUpdateTime;

    public VehicleLocationLog(Long vehicleId, Point coordinate,
                              LocalDateTime lastUpdateTime) {
        this.vehicleId = vehicleId;
        this.coordinate = coordinate;
        this.lastUpdateTime = lastUpdateTime;
    }
}
