package com.ajousw.spring.domain.vehicle.entity;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleStatusRepository extends JpaRepository<VehicleStatus, UUID> {

//    @Modifying
//    @Query("update VehicleStatus vs set vs.usingNavi = :usingNavi, vs.coordinate = :coordinate, " +
//            "vs.meterPerSec = :meterPerSec, vs.direction = :direction, vs.lastUpdateTime = :lastUpdateTime " +
//            "where vs.vehicle.vehicleId = :vehicleId")
//    void updateByVehicleId(@Param("usingNavi") boolean usingNavi,
//                           @Param("coordinate") Point coordinate,
//                           @Param("meterPerSec") double meterPerSec,
//                           @Param("direction") double direction,
//                           @Param("lastUpdateTime") LocalDateTime lastUpdateTime);

    @Modifying(clearAutomatically = true)
    @Query("delete from VehicleStatus vs where vs.vehicle.vehicleId=:vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("select vs from VehicleStatus vs where vs.vehicle.vehicleId=:vehicleId")
    Optional<VehicleStatus> findByVehicleId(@Param("vehicleId") Long vehicleId);
}
