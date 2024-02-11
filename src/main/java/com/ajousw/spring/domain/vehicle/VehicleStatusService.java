package com.ajousw.spring.domain.vehicle;

import com.ajousw.spring.domain.vehicle.entity.VehicleStatus;
import com.ajousw.spring.domain.vehicle.entity.repository.VehicleStatusRepository;
import com.ajousw.spring.socket.handler.message.dto.VehicleStatusUpdateDto;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: 영어로 바꾸기...
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VehicleStatusService {
    private final VehicleStatusRepository vehicleStatusRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public String createVehicleStatus(String sessionId) {
        VehicleStatus vehicleStatus = new VehicleStatus(sessionId, null, false, null, -1, -1,
                LocalDateTime.now(), false);
        vehicleStatusRepository.save(vehicleStatus);

        return vehicleStatus.getVehicleStatusId();
    }

    public void updateVehicleStatus(String vehicleStatusId, VehicleStatusUpdateDto updateDto) {
        VehicleStatus vehicleStatus = findVehicleStatusByVehicleStatusId(vehicleStatusId);
        Point coordinate = geometryFactory.createPoint(
                new Coordinate(updateDto.getLongitude(), updateDto.getLatitude()));

        vehicleStatus.modifyStatus(updateDto.getIsUsingNavi(), coordinate, updateDto.getMeterPerSec(),
                updateDto.getDirection(), updateDto.getLocalDateTime());
    }


    public void deleteVehicleStatus(String vehicleStatusId) {
        vehicleStatusRepository.deleteVehicleStatusByVehicleStatusId(vehicleStatusId);
    }

    private VehicleStatus findVehicleStatusByVehicleStatusId(String sessionId) {
        return vehicleStatusRepository.findVehicleStatusByVehicleStatusId(sessionId).orElseThrow(() -> {
            log.info("Vehicle Status Not Found : [{}]", sessionId);
            return new IllegalArgumentException("Vehicle Status Not Found");
        });
    }

}
