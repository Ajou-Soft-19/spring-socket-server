package com.ajousw.spring.domain.navigation.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PathGuide implements Persistable<PointId> {

    @EmbeddedId
    private PointId pointId;

    @MapsId("navigationPathId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "navigation_path_id", insertable = false, unique = false)
    private NavigationPath navigationPath;

    private Long type;

    private String instructions;

    private Long distance;

    private Long duration;

    public PathGuide(NavigationPath navigationPath, Long pointIndex, Long type, String instructions, Long distance,
                     Long duration) {
        this.pointId = new PointId(navigationPath.getNaviPathId(), pointIndex);
        this.navigationPath = navigationPath;
        this.type = type;
        this.instructions = instructions;
        this.distance = distance;
        this.duration = duration;
    }

    public Long getPointIndex() {
        return this.pointId.getPointIndex();
    }

    @Override
    public PointId getId() {
        return new PointId(this.pointId.getNavigationPathId(), this.pointId.getPointIndex());
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
