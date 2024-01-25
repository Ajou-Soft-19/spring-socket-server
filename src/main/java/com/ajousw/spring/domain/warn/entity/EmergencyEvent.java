package com.ajousw.spring.domain.warn.entity;

import com.ajousw.spring.domain.member.Member;
import com.ajousw.spring.domain.member.repository.BaseTimeEntity;
import com.ajousw.spring.domain.navigation.entity.NavigationPath;
import com.ajousw.spring.domain.vehicle.entity.Vehicle;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmergencyEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emergencyEventId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "navigation_path_id")
    private NavigationPath navigationPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private Boolean isActive;

    private LocalDateTime endedDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "emergencyEvent")
    private final List<WarnRecord> warnRecords = new ArrayList<>();

    public EmergencyEvent(NavigationPath navigationPath, Member member, Vehicle vehicle) {
        this.navigationPath = navigationPath;
        this.member = member;
        this.vehicle = vehicle;
        this.isActive = true;
    }

    public void endEvent() {
        this.isActive = false;
        this.endedDate = LocalDateTime.now();
    }
}
