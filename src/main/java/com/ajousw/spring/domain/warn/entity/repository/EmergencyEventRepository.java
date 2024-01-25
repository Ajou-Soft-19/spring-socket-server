package com.ajousw.spring.domain.warn.entity.repository;

import com.ajousw.spring.domain.member.Member;
import com.ajousw.spring.domain.navigation.entity.NavigationPath;
import com.ajousw.spring.domain.vehicle.entity.Vehicle;
import com.ajousw.spring.domain.warn.entity.EmergencyEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmergencyEventRepository extends JpaRepository<EmergencyEvent, Long> {

    boolean existsByNavigationPath(NavigationPath navigationPath);

    Optional<EmergencyEvent> findByNavigationPath(NavigationPath navigationPath);

    @Query("select e from EmergencyEvent e where e.member=:member and e.vehicle=:vehicle order by e.createdDate desc")
    List<EmergencyEvent> findAllEmergencyEventsOrderByDate(@Param("member") Member member,
                                                           @Param("vehicle") Vehicle vehicle);

    @Query("select e from EmergencyEvent e where e.member=:member and e.vehicle=:vehicle and e.isActive=true order by e.createdDate desc")
    List<EmergencyEvent> findActiveEmergencyEventsOrderByDate(@Param("member") Member member,
                                                              @Param("vehicle") Vehicle vehicle);
}
