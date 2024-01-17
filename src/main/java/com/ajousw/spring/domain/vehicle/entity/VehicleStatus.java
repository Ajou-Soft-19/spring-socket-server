package com.ajousw.spring.domain.vehicle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VehicleStatus {

    // TODO: UUID 값인데 인덱싱으로 성능 저하가 없을지 고려
    @Id
    private String vehicleStatusId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicleId")
    private Vehicle vehicle;

    private boolean usingNavi;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point coordinate;

    private double meterPerSec;

    private double direction;

    private boolean isEmergencyVehicle;

    private LocalDateTime lastUpdateTime;

    @Builder
    public VehicleStatus(String vehicleStatusId, Vehicle vehicle, boolean usingNavi, Point coordinate,
                         double meterPerSec, double direction, LocalDateTime lastUpdateTime, boolean isEmergencyVehicle) {
        this.vehicleStatusId = vehicleStatusId;
        this.vehicle = vehicle;
        this.usingNavi = usingNavi;
        this.coordinate = coordinate;
        this.meterPerSec = meterPerSec;
        this.direction = direction;
        this.lastUpdateTime = lastUpdateTime;
        this.isEmergencyVehicle = isEmergencyVehicle;
    }

    public void modifyStatus(boolean usingNavi, Point coordinate, double meterPerSec,
                             double direction, LocalDateTime lastUpdateTime) {
        this.usingNavi = usingNavi;
        this.coordinate = coordinate;
        this.meterPerSec = meterPerSec;
        this.direction = direction;
        this.lastUpdateTime = lastUpdateTime;
    }
}
