package com.ajousw.spring.domain.vehicle;

import com.ajousw.spring.domain.member.repository.MemberJpaRepository;
import com.ajousw.spring.domain.navigation.NavigationPathUpdater;
import com.ajousw.spring.domain.navigation.api.MapMatcher;
import com.ajousw.spring.domain.vehicle.entity.Vehicle;
import com.ajousw.spring.domain.vehicle.entity.VehicleStatus;
import com.ajousw.spring.domain.vehicle.entity.repository.VehicleRepository;
import com.ajousw.spring.domain.vehicle.entity.repository.VehicleStatusRepository;
import com.ajousw.spring.domain.vehicle.record.GPSRecorder;
import com.ajousw.spring.domain.vehicle.record.LocationData;
import com.ajousw.spring.socket.handler.message.dto.VehicleStatusUpdateDto;
import com.ajousw.spring.socket.handler.service.ContinuousAlertTransmitter;
import java.time.LocalDateTime;
import java.util.Objects;
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
public class EmergencyVehicleStatusService {

    private final VehicleStatusRepository vehicleStatusRepository;
    private final VehicleRepository vehicleRepository;
    private final MemberJpaRepository memberRepository;
    private final NavigationPathUpdater pathUpdater;
    private final ContinuousAlertTransmitter continuousAlertTransmitter;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final MapMatcher mapMatcher;


    public String resetAndCreateVehicleStatus(String sessionId, String email, Long vehicleId) {
        Long memberId = findMemberIdByEmail(email);
        Vehicle vehicle = findVehicleById(vehicleId);
        checkVehicleOwner(memberId, vehicle);
        checkSessionAlreadyExist(vehicle);

        VehicleStatus vehicleStatus = new VehicleStatus(sessionId, vehicle, false,
                null, -1, -1, LocalDateTime.now(), true);
        vehicleStatusRepository.save(vehicleStatus);
        continuousAlertTransmitter.registerTransmitter(vehicleId, vehicle.getLicenceNumber());

        return vehicleStatus.getVehicleStatusId();
    }

    public LocationData updateEmergencyVehicleStatus(String sessionId, Long vehicleId, VehicleStatusUpdateDto updateDto,
                                                     GPSRecorder gpsRecorder) {
        VehicleStatus vehicleStatus = findVehicleStatusByVehicleId(vehicleId);

        LocationData matchedLocation = getMatchedLocation(updateDto, gpsRecorder);
        Point matchedPoint = geometryFactory.createPoint(
                new Coordinate(matchedLocation.getLongitude(), matchedLocation.getLatitude()));
        vehicleStatus.modifyStatus(updateDto.getIsUsingNavi(), matchedPoint, updateDto.getMeterPerSec(),
                updateDto.getDirection(), updateDto.getLocalDateTime());

        if (onEmergencyEvent(updateDto)) {
            continuousAlertTransmitter.broadcastLocation(vehicleId, matchedLocation.getLongitude(),
                    matchedLocation.getLatitude());
            updateCurrentPathPointAndCheckPoint(sessionId, vehicleId, updateDto, matchedLocation);
        }

        return matchedLocation;
    }

    private boolean onEmergencyEvent(VehicleStatusUpdateDto updateDto) {
        return updateDto.getIsUsingNavi() && updateDto.getOnEmergencyEvent()
                && updateDto.getEmergencyEventId() != null;
    }

    private void updateCurrentPathPointAndCheckPoint(String sessionId, Long vehicleId, VehicleStatusUpdateDto updateDto,
                                                     LocationData matchedLocation) {
        pathUpdater.findAndUpdateCurrentPathPoint(sessionId, vehicleId,
                updateDto.getEmergencyEventId(),
                matchedLocation.getLongitude(),
                matchedLocation.getLatitude());
    }

    private LocationData getMatchedLocation(VehicleStatusUpdateDto updateDto, GPSRecorder gpsRecorder) {
        LocationData originalLocation = new LocationData(updateDto.getLatitude(), updateDto.getLongitude(),
                updateDto.getDirection(), updateDto.getLocalDateTime());
        return mapMatcher.requestMapMatchAndRecord(gpsRecorder, originalLocation);
    }

    public void deleteVehicleStatus(Long vehicleId) {
        vehicleStatusRepository.deleteByVehicleId(vehicleId);
        continuousAlertTransmitter.removeTransmitter(vehicleId);
    }

    private void checkSessionAlreadyExist(Vehicle vehicle) {
        if (vehicleStatusRepository.existsByVehicle(vehicle)) {
            throw new IllegalArgumentException("Session Already Exist");
        }
    }

    private void checkVehicleOwner(Long memberId, Vehicle vehicle) {
        if (!Objects.equals(memberId, vehicle.getMember().getId())) {
            throw new IllegalArgumentException("본인이 소유한 차량이 아닙니다.");
        }
    }

    private VehicleStatus findVehicleStatusByVehicleId(Long vehicleId) {
        return vehicleStatusRepository.findByVehicleId(vehicleId).orElseThrow(() -> {
            log.info("Vehicle Status Not Found : [{}]", vehicleId);
            return new IllegalArgumentException("Vehicle Status Not Found");
        });
    }

    private Long findMemberIdByEmail(String email) {
        return memberRepository.findMemberIdByEmail(email).orElseThrow(() -> {
            log.info("No Such member [email:{}]", email);
            return new IllegalArgumentException("No Such member");
        });
    }

    private Vehicle findVehicleById(Long vehicleId) {
        return vehicleRepository.findVehicleByVehicleIdFetchStatus(vehicleId).orElseThrow(() -> {
            log.info("Vehicle Not Found : [{}]", vehicleId);
            return new IllegalArgumentException("Vehicle Not Found");
        });
    }

}
