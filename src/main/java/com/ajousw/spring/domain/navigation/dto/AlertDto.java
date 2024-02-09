package com.ajousw.spring.domain.navigation.dto;

import com.ajousw.spring.domain.vehicle.entity.VehicleType;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlertDto {

    private Long emergencyEventId;

    private Long checkPointId;

    private String licenseNumber;

    private VehicleType vehicleType;

    private Long currentPathPoint;

    private List<PathPointDto> pathPoints;

    public AlertDto(Long emergencyEventId, Long checkPointId, String licenseNumber, VehicleType vehicleType,
                    Long currentPathPoint, List<PathPointDto> pathPoints) {
        this.emergencyEventId = emergencyEventId;
        this.checkPointId = checkPointId;
        this.licenseNumber = licenseNumber;
        this.vehicleType = vehicleType;
        this.currentPathPoint = currentPathPoint;
        this.pathPoints = pathPoints;
    }
}
