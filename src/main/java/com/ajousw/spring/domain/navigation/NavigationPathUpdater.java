package com.ajousw.spring.domain.navigation;

import com.ajousw.spring.domain.exception.BadApiResponseException;
import com.ajousw.spring.domain.navigation.api.OsrmTableService;
import com.ajousw.spring.domain.navigation.dto.TableQueryResultDto;
import com.ajousw.spring.domain.navigation.entity.NavigationPath;
import com.ajousw.spring.domain.navigation.entity.PathPoint;
import com.ajousw.spring.domain.util.CoordinateUtil;
import com.ajousw.spring.domain.warn.entity.EmergencyEvent;
import com.ajousw.spring.domain.warn.entity.repository.EmergencyEventRepository;
import com.ajousw.spring.socket.handler.message.dto.CurrentPointUpdateDto;
import com.ajousw.spring.socket.handler.pubsub.RedisMessagePublisher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, noRollbackFor = BadApiResponseException.class)
public class NavigationPathUpdater {

    private final EmergencyEventRepository emergencyEventRepository;
    private final RedisMessagePublisher redisMessagePublisher;
    private final OsrmTableService tableService;
    private final Long MAX_DISTANCE = 50L;

    @Async
    public void findAndUpdateCurrentPathPoint(String sessionId, Long vehicleId, Long emergencyEventId,
                                              double longitude, double latitude, double direction) {
        EmergencyEvent emergencyEvent = findEmergencyEventById(emergencyEventId);
        validateEmergencyEventId(vehicleId, emergencyEvent);
        NavigationPath navigationPath = emergencyEvent.getNavigationPath();

        Optional<PathPoint> closestPathPoint = findClosestPathPoint(sessionId, navigationPath, longitude, latitude,
                direction, navigationPath.getCurrentPathPoint());
        CurrentPointUpdateDto currentPointUpdateDto = createCurrentPointUpdateDto(closestPathPoint, navigationPath,
                emergencyEventId, longitude, latitude);

        log.info("<{}> update idx from {}, to {}", sessionId.substring(0, 13), navigationPath.getCurrentPathPoint(),
                currentPointUpdateDto.getCurrentPathPoint());
        redisMessagePublisher.publicPointUpdateMessageToSocket(currentPointUpdateDto);
    }

    private Optional<PathPoint> findClosestPathPoint(String sessionId, NavigationPath navigationPath, double currentLon,
                                                     double currentLat,
                                                     double direction, Long currentPathPointIndex) {
        List<PathPoint> pathPoints = navigationPath.getPathPoints();
        PathPoint closestPoint = null;
        double closestDistance = Double.MAX_VALUE;

        if (pathPoints.size() <= 1) {
            return Optional.empty();
        }

        for (PathPoint point : pathPoints) {
            double distance = CoordinateUtil.calculateDistance(currentLat, currentLon,
                    point.getCoordinate().getY(), point.getCoordinate().getX());

            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = point;
            }
        }

        if (closestPoint == null || closestPoint.getPointIndex() <= currentPathPointIndex) {
            return Optional.empty();
        }

        // TODO: path point 확인 정확도 향상을 위해 파라미터 조정 1.2배?
        if (closestDistance > MAX_DISTANCE) {
            return Optional.empty();
        }

        if (!validateClosestPoint(sessionId, closestPoint, currentLon, currentLat, direction)) {
            return Optional.empty();
        }

        return Optional.of(closestPoint);
    }

    private boolean validateClosestPoint(String sessionId, PathPoint closestPoint,
                                         double currentLon, double currentLat, double direction) {
        try {
            List<TableQueryResultDto> resultDtos = tableService.getTableOfMultiDestDistanceAndDurationWithHeading(
                    getSource(currentLon, currentLat), getDestinations(List.of(closestPoint)), List.of(direction));
            TableQueryResultDto resultDto = getClosestPathPoint(resultDtos);

            return resultDto.getDistance() <= MAX_DISTANCE;
        } catch (Exception e) {
            log.info("<{}> Cannot find way to pathPoint {}, lon: {}, lat: {}", sessionId.substring(0, 13),
                    closestPoint.getPointId(), currentLon, currentLat);
            return true;
        }
    }

    private TableQueryResultDto getClosestPathPoint(List<TableQueryResultDto> resultDtos) {
        return resultDtos.get(0);
    }

    private CurrentPointUpdateDto createCurrentPointUpdateDto(Optional<PathPoint> closestPathPoint,
                                                              NavigationPath navigationPath,
                                                              Long emergencyEventId, double longitude,
                                                              double latitude) {
        long pathPointIndex = closestPathPoint.map(PathPoint::getPointIndex)
                .orElse(navigationPath.getCurrentPathPoint());

        return new CurrentPointUpdateDto(navigationPath.getNaviPathId(), pathPointIndex,
                emergencyEventId, longitude, latitude);
    }

    private void validateEmergencyEventId(Long vehicleId, EmergencyEvent emergencyEvent) {
        if (!Objects.equals(emergencyEvent.getVehicle().getVehicleId(), vehicleId)) {
            throw new IllegalArgumentException("Not Correct EmergencyEvent, VehicleId Pair");
        }
    }

    private String getSource(double lon, double lat) {
        return lon + "," + lat;
    }

    private List<String> getDestinations(List<PathPoint> pathPoints) {
        List<String> destinations = new ArrayList<>();
        for (PathPoint pathPoint : pathPoints) {
            destinations.add(pointToString(pathPoint.getCoordinate()));
        }
        return destinations;
    }

    private String pointToString(Point point) {
        return point.getX() + "," + point.getY();
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

}
