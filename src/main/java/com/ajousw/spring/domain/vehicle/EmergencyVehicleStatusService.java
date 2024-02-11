package com.ajousw.spring.domain.vehicle;

import com.ajousw.spring.domain.member.repository.MemberJpaRepository;
import com.ajousw.spring.domain.navigation.entity.NavigationPath;
import com.ajousw.spring.domain.navigation.entity.PathPoint;
import com.ajousw.spring.domain.util.CoordinateUtil;
import com.ajousw.spring.domain.vehicle.entity.Vehicle;
import com.ajousw.spring.domain.vehicle.entity.VehicleStatus;
import com.ajousw.spring.domain.vehicle.entity.repository.VehicleRepository;
import com.ajousw.spring.domain.vehicle.entity.repository.VehicleStatusRepository;
import com.ajousw.spring.domain.vehicle.record.GPSRecorder;
import com.ajousw.spring.domain.vehicle.record.LocationData;
import com.ajousw.spring.domain.warn.entity.EmergencyEvent;
import com.ajousw.spring.domain.warn.entity.repository.EmergencyEventRepository;
import com.ajousw.spring.socket.handler.message.dto.CurrentPointUpdateDto;
import com.ajousw.spring.socket.handler.message.dto.VehicleStatusUpdateDto;
import com.ajousw.spring.socket.handler.pubsub.ContinuousAlertTransmitter;
import com.ajousw.spring.socket.handler.pubsub.RedisMessagePublisher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private final EmergencyEventRepository emergencyEventRepository;
    private final VehicleStatusRepository vehicleStatusRepository;
    private final VehicleRepository vehicleRepository;
    private final MemberJpaRepository memberRepository;
    private final RedisMessagePublisher redisMessagePublisher;
    private final ContinuousAlertTransmitter continuousAlertTransmitter;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final MapMatcher mapMatcher;
    private final Long MAX_DISTANCE = 50L;

    public String resetAndCreateVehicleStatus(String sessionId, String email, Long vehicleId) {
        Long memberId = findMemberIdByEmail(email);
        Vehicle vehicle = findVehicleById(vehicleId);

        if (!Objects.equals(memberId, vehicle.getMember().getId())) {
            throw new IllegalArgumentException("본인이 소유한 차량이 아닙니다.");
        }

        if (vehicleStatusRepository.existsByVehicle(vehicle)) {
            throw new IllegalArgumentException("Session Already Exist");
        }

        VehicleStatus vehicleStatus = new VehicleStatus(sessionId, vehicle, false, null, -1, -1, LocalDateTime.now(),
                true);
        vehicleStatusRepository.save(vehicleStatus);
        continuousAlertTransmitter.registerTransmitter(vehicleId, vehicle.getLicenceNumber());
        return vehicleStatus.getVehicleStatusId();
    }

    public LocationData updateEmergencyVehicleStatus(String email, Long vehicleId,
                                                     VehicleStatusUpdateDto updateDto, GPSRecorder gpsRecorder) {
        VehicleStatus vehicleStatus = findVehicleStatusByVehicleId(vehicleId);
        LocationData matchedLocation = getMatchedLocation(updateDto, gpsRecorder);
        Point currnetPoint = geometryFactory.createPoint(
                new Coordinate(matchedLocation.getLongitude(), matchedLocation.getLatitude()));
        
        vehicleStatus.modifyStatus(updateDto.getIsUsingNavi(), currnetPoint, updateDto.getMeterPerSec(),
                updateDto.getDirection(), updateDto.getLocalDateTime());

        if (!updateDto.getIsUsingNavi() || !updateDto.getOnEmergencyEvent()
                || updateDto.getEmergencyEventId() == null) {
            return matchedLocation;
        }

        continuousAlertTransmitter.broadcastLocation(vehicleId, matchedLocation.getLongitude(),
                matchedLocation.getLatitude());

        findAndUpdateCurrentPathPoint(email, vehicleId,
                updateDto.getEmergencyEventId(),
                matchedLocation.getLongitude(),
                matchedLocation.getLatitude());

        return matchedLocation;
    }

    private LocationData getMatchedLocation(VehicleStatusUpdateDto updateDto, GPSRecorder gpsRecorder) {
        LocationData originalLocation = new LocationData(updateDto.getLatitude(), updateDto.getLongitude(),
                updateDto.getDirection(), updateDto.getLocalDateTime());
        return mapMatcher.requestMapMatchAndRecord(gpsRecorder, originalLocation);
    }

    private Optional<String> findAndUpdateCurrentPathPoint(String email, Long vehicleId, Long emergencyEventId,
                                                           double longitude, double latitude) {
        EmergencyEvent emergencyEvent = findEmergencyEventById(emergencyEventId);
        NavigationPath navigationPath = emergencyEvent.getNavigationPath();

        if (!Objects.equals(emergencyEvent.getVehicle().getVehicleId(), vehicleId)) {
            throw new IllegalArgumentException("Not Correct EmergencyEvent, VehicleId Pair");
        }

        List<PathPoint> pathPoints = navigationPath.getPathPoints();

        Optional<PathPoint> closestPathPoint = findClosestPathPoint(pathPoints, longitude, latitude,
                navigationPath.getCurrentPathPoint());

        CurrentPointUpdateDto currentPointUpdateDto;
        if (closestPathPoint.isEmpty()) {
            currentPointUpdateDto = new CurrentPointUpdateDto(navigationPath.getNaviPathId(),
                    navigationPath.getCurrentPathPoint(),
                    emergencyEventId, email, longitude, latitude);
            redisMessagePublisher.publicPointUpdateMessageToSocket(currentPointUpdateDto);
            return Optional.empty();
        }

        log.info("update idx from {}, to {}", navigationPath.getCurrentPathPoint(),
                closestPathPoint.get().getPointIndex());

        currentPointUpdateDto = new CurrentPointUpdateDto(navigationPath.getNaviPathId(),
                closestPathPoint.get().getPointIndex(), emergencyEventId, email, longitude, latitude);
        redisMessagePublisher.publicPointUpdateMessageToSocket(currentPointUpdateDto);
        return Optional.of(String.format("Passed pathPoint %d", closestPathPoint.get().getPointIndex()));
    }

    private Optional<PathPoint> findClosestPathPoint(List<PathPoint> pathPoints, double longitude, double latitude,
                                                     Long currentPathPointIndex) {
        PathPoint closestPoint = null;
        double closestDistance = Double.MAX_VALUE;

        if (pathPoints.size() <= 1) {
            return Optional.empty();
        }

        for (PathPoint point : pathPoints) {
            double distance = CoordinateUtil.calculateDistance(latitude, longitude, point.getCoordinate().getY(),
                    point.getCoordinate().getX());

            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = point;
            }
        }

        if (closestPoint == null || closestPoint.getPointIndex() <= currentPathPointIndex) {
            return Optional.empty();
        }

        // TODO: path point 확인 정확도 향상 로직 구현
        if (closestDistance > MAX_DISTANCE) {
            return Optional.empty();
        }

        return Optional.of(closestPoint);
    }

    public void deleteVehicleStatus(Long vehicleId) {
        vehicleStatusRepository.deleteByVehicleId(vehicleId);
        continuousAlertTransmitter.removeTransmitter(vehicleId);
    }

    private EmergencyEvent findEmergencyEventById(Long emergencyPathId) {
        EmergencyEvent emergencyEvent = emergencyEventRepository.findById(emergencyPathId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No Such EmergencyEvent"));

        if (!emergencyEvent.getIsActive()) {
            throw new IllegalStateException("EmergencyEvent Already Ended");
        }

        return emergencyEvent;
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
