package com.ajousw.spring.domain.navigation.dto;

import com.ajousw.spring.domain.navigation.entity.PathGuide;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PathGuideDto {
    private Long pointIndex;
    private Long type;
    private Long distance;
    private Long duration;
    private String instructions;

    public PathGuideDto(PathGuide pathGuide) {
        this.pointIndex = pathGuide.getPointIndex();
        this.type = pathGuide.getType();
        this.distance = pathGuide.getDistance();
        this.duration = pathGuide.getDuration();
        this.instructions = pathGuide.getInstructions();
    }
}
