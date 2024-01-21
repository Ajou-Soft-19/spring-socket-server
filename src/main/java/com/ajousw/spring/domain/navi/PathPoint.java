package com.ajousw.spring.domain.navi;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PathPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID pathPointId;

    private Long navigationPathId;

    private Long index;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point coordinate;

}
