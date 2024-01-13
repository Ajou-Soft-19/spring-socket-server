package com.ajousw.spring.domain.vehicle.entity;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("select exists (select v from Vehicle v where v.vehicleId=:vehicleId and v.member.id=:memberId)")
    boolean existsByVehicleIdAndMemberId(@Param("vehicleId") Long vehicleId, @Param("memberId") Long memberId);

    @Query("select v from Vehicle v left join fetch v.vehicleStatus where v.vehicleId=:vehicleId")
    Optional<Vehicle> findVehicleByVehicleIdFetchStatus(@Param("vehicleId") Long vehicleId);
}
