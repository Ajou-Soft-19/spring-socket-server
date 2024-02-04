package com.ajousw.spring.domain.vehicle;

import com.ajousw.spring.domain.member.repository.MemberJpaRepository;
import com.ajousw.spring.domain.navigation.entity.NavigationPath;
import com.ajousw.spring.domain.navigation.entity.PathPoint;
import com.ajousw.spring.domain.navigation.entity.repository.NavigationPathRepository;
import com.ajousw.spring.domain.vehicle.entity.Vehicle;
import com.ajousw.spring.domain.vehicle.entity.VehicleLocationLog;
import com.ajousw.spring.domain.vehicle.entity.VehicleStatus;
import com.ajousw.spring.domain.vehicle.entity.repository.VehicleLocationLogRepository;
import com.ajousw.spring.domain.vehicle.entity.repository.VehicleRepository;
import com.ajousw.spring.domain.vehicle.entity.repository.VehicleStatusRepository;
import com.ajousw.spring.domain.warn.entity.EmergencyEvent;
import com.ajousw.spring.domain.warn.entity.repository.EmergencyEventRepository;
import com.ajousw.spring.socket.handler.message.dto.CurrentPointUpdateDto;
import com.ajousw.spring.socket.handler.message.dto.VehicleStatusUpdateDto;
import com.ajousw.spring.socket.handler.pubsub.RedisMessagePublisher;
import com.ajousw.spring.util.CoordinateUtil;
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
    private final VehicleLocationLogRepository vehicleLocationLogRepository;
    private final NavigationPathRepository navigationPathRepository;
    private final EmergencyEventRepository emergencyEventRepository;
    private final VehicleStatusRepository vehicleStatusRepository;
    private final VehicleRepository vehicleRepository;
    private final MemberJpaRepository memberRepository;
    private final RedisMessagePublisher redisMessagePublisher;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
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

        return vehicleStatus.getVehicleStatusId();
    }

    public Optional<String> updateEmergencyVehicleStatus(String email, Long vehicleId,
                                                         VehicleStatusUpdateDto updateDto) {
        VehicleStatus vehicleStatus = findVehicleStatusByVehicleId(vehicleId);
        Point coordinate = geometryFactory.createPoint(
                new Coordinate(updateDto.getLongitude(), updateDto.getLatitude()));

        vehicleStatus.modifyStatus(updateDto.getIsUsingNavi(), coordinate, updateDto.getMeterPerSec(),
                updateDto.getDirection(), updateDto.getLocalDateTime());

        if (!updateDto.getIsUsingNavi() || !updateDto.getOnEmergencyEvent()
                || updateDto.getEmergencyEventId() == null) {
            return Optional.empty();
        }

        return findAndUpdateCurrentPathPoint(email, vehicleId,
                updateDto.getEmergencyEventId(),
                updateDto.getLongitude(),
                updateDto.getLatitude());
    }

    private void logVehicleLocation(Long vehicleId, Point coordinate, LocalDateTime lastUpdateTime) {
        VehicleLocationLog vehicleLocationLog = new VehicleLocationLog(vehicleId, coordinate, lastUpdateTime);
        vehicleLocationLogRepository.save(vehicleLocationLog);
    }

    // TODO: 사용자, 차량 검증 로직 추가
    private Optional<String> findAndUpdateCurrentPathPoint(String email, Long vehicleId, Long emergencyEventId,
                                                           double longitude, double latitude) {
        EmergencyEvent emergencyEvent = findEmergencyEventById(emergencyEventId);
        NavigationPath navigationPath = emergencyEvent.getNavigationPath();

//        if (!Objects.equals(emergencyEvent.getNavigationPath().getNaviPathId(), naviPathId)) {
//            throw new IllegalArgumentException("Not Correct EmergencyEvent, NavigationPath Pair");
//        }

        if (!Objects.equals(emergencyEvent.getVehicle().getVehicleId(), vehicleId)) {
            throw new IllegalArgumentException("Not Correct EmergencyEvent, VehicleId Pair");
        }

        List<PathPoint> pathPoints = navigationPath.getPathPoints();

        Optional<PathPoint> closestPathPoint = findClosestPathPoint(pathPoints, longitude, latitude,
                navigationPath.getCurrentPathPoint());

        if (closestPathPoint.isEmpty()) {
            return Optional.empty();
        }

        log.info("update idx from {}, to {}", navigationPath.getCurrentPathPoint(),
                closestPathPoint.get().getPointIndex());

        CurrentPointUpdateDto currentPointUpdateDto = new CurrentPointUpdateDto(navigationPath.getNaviPathId(),
                closestPathPoint.get().getPointIndex(), emergencyEventId, email);

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

    private EmergencyEvent findEmergencyEventByNaviPath(NavigationPath navigationPath) {
        return emergencyEventRepository.findByNavigationPath(navigationPath)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No Such EmergencyEvent"));
    }

    private NavigationPath findNavigationPathById(Long naviPathId) {
        return navigationPathRepository.findNavigationPathByNaviPathId(naviPathId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No Such NavigationPath"));
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
