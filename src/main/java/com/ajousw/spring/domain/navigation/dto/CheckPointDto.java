package com.ajousw.spring.domain.navigation.dto;

import com.ajousw.spring.domain.navigation.entity.CheckPoint;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckPointDto {

    private Long pointIndex;
    private List<Double> location;
    private Double distance;
    private Double duration;

    public CheckPointDto(CheckPoint checkPoint) {
        this.pointIndex = checkPoint.getPointIndex();
        this.location = List.of(checkPoint.getCoordinate().getX(), checkPoint.getCoordinate().getY());
        this.distance = checkPoint.getDistance();
        this.duration = checkPoint.getDuration();
    }

    public CheckPointDto(CheckPoint checkPoint, double duration) {
        this.pointIndex = checkPoint.getPointIndex();
        this.location = List.of(checkPoint.getCoordinate().getX(), checkPoint.getCoordinate().getY());
        this.distance = checkPoint.getDistance();
        this.duration = duration;
    }
}
