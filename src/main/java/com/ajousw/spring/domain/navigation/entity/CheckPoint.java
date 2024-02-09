package com.ajousw.spring.domain.navigation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckPoint implements Persistable<PointId> {

    @EmbeddedId
    private PointId pointId;

    @MapsId("navigationPathId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "navigation_path_id", insertable = false, unique = false)
    private NavigationPath navigationPath;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point coordinate;

    private Double distance;

    private Double duration;

    public CheckPoint(NavigationPath navigationPath, Point coordinate, Long pointIndex, Double distance,
                      Double duration) {
        this.pointId = new PointId(navigationPath.getNaviPathId(), pointIndex);
        this.navigationPath = navigationPath;
        this.coordinate = coordinate;
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

