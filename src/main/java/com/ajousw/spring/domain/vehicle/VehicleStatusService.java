package com.ajousw.spring.domain.vehicle;

import com.ajousw.spring.domain.navigation.api.MapMatcher;
import com.ajousw.spring.domain.vehicle.entity.VehicleStatus;
import com.ajousw.spring.domain.vehicle.entity.repository.VehicleStatusRepository;
import com.ajousw.spring.domain.vehicle.record.GPSRecorder;
import com.ajousw.spring.domain.vehicle.record.LocationData;
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
    private final MapMatcher mapMatcher;

    public String createVehicleStatus(String sessionId) {
        VehicleStatus vehicleStatus = new VehicleStatus(sessionId, null, false, null, -1, -1,
                LocalDateTime.now(), false);
        vehicleStatusRepository.save(vehicleStatus);

        return vehicleStatus.getVehicleStatusId();
    }

    public LocationData updateVehicleStatus(String vehicleStatusId, VehicleStatusUpdateDto updateDto,
                                            GPSRecorder gpsRecorder) {
        VehicleStatus vehicleStatus = findVehicleStatusByVehicleStatusId(vehicleStatusId);
        LocationData matchedLocation = getMatchedLocation(updateDto, gpsRecorder);
        Point matchedPoint = geometryFactory.createPoint(
                new Coordinate(matchedLocation.getLongitude(), matchedLocation.getLatitude()));
        vehicleStatus.modifyStatus(updateDto.getIsUsingNavi(), matchedPoint, updateDto.getMeterPerSec(),
                updateDto.getDirection(), updateDto.getLocalDateTime());

        return matchedLocation;
    }

    private LocationData getMatchedLocation(VehicleStatusUpdateDto updateDto, GPSRecorder gpsRecorder) {
        LocationData originalLocation = new LocationData(updateDto.getLatitude(), updateDto.getLongitude(),
                updateDto.getDirection(), updateDto.getLocalDateTime());
        return mapMatcher.requestMapMatchAndRecord(gpsRecorder, originalLocation);
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
