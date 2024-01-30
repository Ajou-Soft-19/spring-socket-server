package com.ajousw.spring.domain.warn.entity;

import com.ajousw.spring.domain.vehicle.entity.VehicleStatus;
import com.ajousw.spring.domain.warn.entity.WarnRecord.WarnRecordId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarnRecord implements Persistable<WarnRecordId> {

    @EmbeddedId
    private WarnRecordId warnRecordId;

    @MapsId("emergencyEventId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_event_id")
    private EmergencyEvent emergencyEvent;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point coordinate;

    private Double meterPerSec;

    private Double direction;

    private Boolean usingNavi;

    private LocalDateTime createdDate;

    public WarnRecord(EmergencyEvent emergencyEvent, Long checkPointIndex, VehicleStatus vehicleStatus) {
        this.warnRecordId = new WarnRecordId(emergencyEvent.getEmergencyEventId(), checkPointIndex,
                vehicleStatus.getVehicleStatusId());
        this.emergencyEvent = emergencyEvent;
        this.coordinate = vehicleStatus.getCoordinate();
        this.meterPerSec = vehicleStatus.getMeterPerSec();
        this.direction = vehicleStatus.getDirection();
        this.usingNavi = vehicleStatus.isUsingNavi();
        this.createdDate = LocalDateTime.now();
    }

    @Getter
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class WarnRecordId implements Serializable {

        private Long emergencyEventId;

        private Long checkPointIndex;

        private String sessionId;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            WarnRecordId that = (WarnRecordId) o;
            return Objects.equals(emergencyEventId, that.emergencyEventId)
                    && Objects.equals(checkPointIndex, that.checkPointIndex)
                    && Objects.equals(sessionId, that.sessionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(emergencyEventId, checkPointIndex, sessionId);
        }
    }

    @Override
    public WarnRecordId getId() {
        return new WarnRecordId(this.warnRecordId.getEmergencyEventId(), this.warnRecordId.getCheckPointIndex(),
                this.warnRecordId.sessionId);
    }

    // 그냥 True? 어차피 수정할일 없으니까
    @Override
    public boolean isNew() {
//        return this.getCreatedDate() == null;
        return true;
    }
}