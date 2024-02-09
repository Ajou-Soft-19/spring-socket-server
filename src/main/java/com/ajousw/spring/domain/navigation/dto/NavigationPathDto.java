package com.ajousw.spring.domain.navigation.dto;

import com.ajousw.spring.domain.navigation.api.provider.Provider;
import com.ajousw.spring.domain.navigation.entity.MapLocation;
import com.ajousw.spring.domain.navigation.entity.NavigationPath;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NavigationPathDto {
    private Long naviPathId;
    private Long vehicleId;
    private boolean isEmergencyPath;
    private Provider provider;
    private MapLocation sourceLocation;
    private MapLocation destLocation;
    private String queryType;
    private Long distance;
    private Long duration;
    private Long pathPointSize;
    private Long currentPathPoint;
    private Long currentCheckPoint;
    private List<PathPointDto> pathPoint;
    private List<PathGuideDto> pathGuide;
    private List<CheckPointDto> checkPoint;

    // 일반 차량용 DTO
    public NavigationPathDto(NavigationPath navigationPath, List<PathPointDto> pathPoints,
                             List<PathGuideDto> pathGuides) {
        this.naviPathId = null;
        this.vehicleId = null;
        this.isEmergencyPath = false;
        this.provider = navigationPath.getProvider();
        this.sourceLocation = navigationPath.getSourceLocation();
        this.destLocation = navigationPath.getDestLocation();
        this.queryType = navigationPath.getQueryType();
        this.distance = navigationPath.getDistance();
        this.duration = navigationPath.getDuration();
        this.currentPathPoint = navigationPath.getCurrentPathPoint();
        this.currentCheckPoint = navigationPath.getCurrentCheckPoint();
        this.pathPointSize = ((Integer) pathPoints.size()).longValue();
        this.pathGuide = pathGuides;
        this.pathPoint = pathPoints;
    }

    // 응급 차량용 DTO
    public NavigationPathDto(NavigationPath navigationPath, List<PathPointDto> pathPoints,
                             List<CheckPointDto> checkPoints, boolean isEmergencyPath) {
        this.naviPathId = navigationPath.getNaviPathId();
        this.vehicleId = navigationPath.getVehicle().getVehicleId();
        this.provider = navigationPath.getProvider();
        this.isEmergencyPath = isEmergencyPath;
        this.sourceLocation = navigationPath.getSourceLocation();
        this.destLocation = navigationPath.getDestLocation();
        this.queryType = navigationPath.getQueryType();
        this.distance = navigationPath.getDistance();
        this.duration = navigationPath.getDuration();
        this.currentPathPoint = navigationPath.getCurrentPathPoint();
        this.currentCheckPoint = navigationPath.getCurrentCheckPoint();
        this.pathPointSize = ((Integer) pathPoints.size()).longValue();
        this.pathPoint = pathPoints;
        this.checkPoint = checkPoints;
    }

}
