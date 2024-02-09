package com.ajousw.spring.domain.navigation.dto;

import com.ajousw.spring.domain.navigation.entity.PathPoint;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PathPointDto {
    private Long index;
    private List<Double> location;

    public PathPointDto(PathPoint pathPoint) {
        this.index = pathPoint.getPointIndex();
        this.location = List.of(pathPoint.getCoordinate().getX(), pathPoint.getCoordinate().getY());
    }

}
