package com.ajousw.spring.domain.vehicle;

import com.ajousw.spring.domain.member.repository.MemberJpaRepository;
import com.ajousw.spring.domain.navigation.dto.PathPointDto;
import com.ajousw.spring.domain.navigation.entity.CheckPoint;
import com.ajousw.spring.domain.navigation.entity.MapLocation;
import com.ajousw.spring.domain.navigation.entity.NavigationPath;
import com.ajousw.spring.domain.navigation.entity.PathPoint;
import com.ajousw.spring.domain.vehicle.entity.Vehicle;
import com.ajousw.spring.domain.vehicle.entity.VehicleStatus;
import com.ajousw.spring.domain.vehicle.entity.repository.VehicleRepository;
import com.ajousw.spring.domain.vehicle.entity.repository.VehicleStatusRepository;
import com.ajousw.spring.domain.warn.entity.EmergencyEvent;
import com.ajousw.spring.domain.warn.entity.repository.EmergencyEventRepository;
import com.ajousw.spring.socket.handler.message.dto.CurrentPointUpdateDto;
import com.ajousw.spring.socket.handler.message.dto.VehicleStatusUpdateDto;
import com.ajousw.spring.socket.handler.pubsub.RedisMessagePublisher;
import com.ajousw.spring.util.CoordinateUtil;
import java.time.LocalDateTime;
import java.util.Comparator;
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

        return updateCurrentPathPoint(email, vehicleId,
                updateDto.getEmergencyEventId(),
                updateDto.getLongitude(),
                updateDto.getLatitude());
    }

    private Optional<String> updateCurrentPathPoint(String email, Long vehicleId, Long emergencyEventId,
                                                    double longitude, double latitude) {
        EmergencyEvent emergencyEvent = findEmergencyEventById(emergencyEventId);
        NavigationPath navigationPath = emergencyEvent.getNavigationPath();

        if (!Objects.equals(emergencyEvent.getVehicle().getVehicleId(), vehicleId)) {
            throw new IllegalArgumentException("Not Correct EmergencyEvent, VehicleId Pair");
        }

        List<PathPoint> pathPoints = navigationPath.getPathPoints();

        Optional<PathPoint> closestPathPoint = findClosestPathPoint(pathPoints, longitude, latitude,
                navigationPath.getCurrentPathPoint());

        if (closestPathPoint.isEmpty()) {
            return Optional.empty();
        }
        Long curPathIdx = closestPathPoint.get().getPointIndex();
        Long oldPathIdx = navigationPath.getCurrentPathPoint();

        navigationPath.updateCurrentPathPoint(curPathIdx);
        log.info("update idx from {}, to {}", oldPathIdx, curPathIdx);

        CurrentPointUpdateDto currentPointUpdateDto = new CurrentPointUpdateDto(navigationPath.getNaviPathId(),
                closestPathPoint.get().getPointIndex(), emergencyEventId, email);

        redisMessagePublisher.publicPointUpdateMessageToSocket(currentPointUpdateDto);
        return Optional.of(String.format("Passed pathPoint %d", closestPathPoint.get().getPointIndex()));
    }

    private void updateNextCheckPoint(NavigationPath navigationPath,
                                      Long emergencyEventId,
                                      Long oldPathIdx,
                                      Long curPathIdx,
                                      List<CheckPoint> checkPoints) {
        Optional<CheckPoint> nextCheckPointOptional = findNextCheckPoint(curPathIdx, oldPathIdx,
                checkPoints);
        CheckPoint nextCheckPoint;
        // 체크포인트가 하나도 없는 경우를 처리하기 위한 예외
        nextCheckPoint = nextCheckPointOptional.orElseGet(() -> checkPoints.stream()
                .filter(c -> c.getPointIndex() <= curPathIdx)
                .max(Comparator.comparing(CheckPoint::getPointIndex))
                .orElse(null));

        if (nextCheckPoint == null) {
            return;
        }
        navigationPath.updateCheckPoint(nextCheckPoint.getPointIndex());

        List<PathPointDto> filteredPathPoints = navigationPath.getPathPoints().stream()
                .filter(p -> filterPathInCheckPoint(curPathIdx, nextCheckPoint, p))
                .map(PathPointDto::new).toList();
    }

    private Optional<CheckPoint> findNextCheckPoint(Long curPathIdx, Long oldPathIdx, List<CheckPoint> checkPoints) {
        Optional<CheckPoint> previousCheckPointOptional = checkPoints.stream()
                .filter(c -> c.getPointIndex() > oldPathIdx && c.getPointIndex() <= curPathIdx)
                .max(Comparator.comparing(CheckPoint::getPointIndex));

        if (previousCheckPointOptional.isEmpty()) {
            return Optional.empty();
        }

        CheckPoint previousCheckPoint = previousCheckPointOptional.get();

        return checkPoints.stream()
                .filter(c -> c.getPointIndex() > previousCheckPoint.getPointIndex())
                .min(Comparator.comparing(CheckPoint::getPointIndex));
    }

    private boolean filterPathInCheckPoint(Long curPathIdx, CheckPoint nextCheckPoint, PathPoint targetPathPoint) {
        if (curPathIdx <= targetPathPoint.getPointIndex()
                && targetPathPoint.getPointIndex() <= nextCheckPoint.getPointIndex()) {
            return true;
        }
        MapLocation checkPointLocation
                = new MapLocation(nextCheckPoint.getCoordinate().getY(), nextCheckPoint.getCoordinate().getX());
        MapLocation pathPointLocation
                = new MapLocation(targetPathPoint.getCoordinate().getY(), targetPathPoint.getCoordinate().getX());
        double distance = CoordinateUtil.calculateDistance(checkPointLocation, pathPointLocation);

        return distance <= checkPointRadius;
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
