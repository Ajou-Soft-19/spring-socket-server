package com.ajousw.spring.domain.navigation.entity;

import com.ajousw.spring.domain.member.Member;
import com.ajousw.spring.domain.member.repository.BaseTimeEntity;
import com.ajousw.spring.domain.navigation.api.provider.Provider;
import com.ajousw.spring.domain.vehicle.entity.Vehicle;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NavigationPath extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long naviPathId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private Boolean isEmergencyPath;

    @Enumerated
    private Provider provider;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "source_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "source_longitude"))
    })
    private MapLocation sourceLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "dest_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "dest_longitude"))
    })
    private MapLocation destLocation;

    private String queryType;

    private Long distance;

    private Long duration;

    private Long currentPathPoint;

    private Long currentCheckPoint;

    @OneToMany(mappedBy = "navigationPath", fetch = FetchType.LAZY)
    private final List<PathPoint> pathPoints = new ArrayList<>();

    @OneToMany(mappedBy = "navigationPath", fetch = FetchType.LAZY)
    private final List<PathGuide> guides = new ArrayList<>();

    @OneToMany(mappedBy = "navigationPath", fetch = FetchType.LAZY)
    private final List<CheckPoint> checkPoints = new ArrayList<>();

    private Long pathPointSize;

    @Builder
    public NavigationPath(Member member, Vehicle vehicle, Boolean isEmergencyPath, Provider provider,
                          MapLocation sourceLocation,
                          MapLocation destLocation,
                          String queryType, Long distance, Long duration, Long currentPathPoint, Long pathPointSize) {
        this.member = member;
        this.vehicle = vehicle;
        this.isEmergencyPath = isEmergencyPath;
        this.provider = provider;
        this.sourceLocation = sourceLocation;
        this.destLocation = destLocation;
        this.queryType = queryType;
        this.distance = distance;
        this.duration = duration;
        this.currentPathPoint = currentPathPoint;
        this.currentCheckPoint = 0L;
        this.pathPointSize = pathPointSize;
    }

    public void updateCurrentPathPoint(Long currentIdx) {
        if (currentIdx < 0 || this.pathPointSize <= currentIdx) {
            throw new IllegalArgumentException(String.format("Wrong Index Range Not in [0 ~ %d]", pathPointSize - 1));
        }

        if (this.currentPathPoint >= currentIdx) {
            return;
        }

        this.currentPathPoint = currentIdx;
    }

    public void updateCheckPoint(Long currentIdx) {
        if (this.currentCheckPoint >= currentIdx) {
            return;
        }
        this.currentCheckPoint = currentIdx;
    }
}

