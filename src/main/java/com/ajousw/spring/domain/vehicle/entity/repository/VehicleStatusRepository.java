package com.ajousw.spring.domain.vehicle.entity.repository;

import com.ajousw.spring.domain.vehicle.entity.Vehicle;
import com.ajousw.spring.domain.vehicle.entity.VehicleStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleStatusRepository extends JpaRepository<VehicleStatus, UUID> {

    @Modifying(clearAutomatically = true)
    @Query("delete from VehicleStatus vs where vs.vehicle.vehicleId=:vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);

    boolean existsByVehicle(Vehicle vehicle);

    @Query("select vs from VehicleStatus vs where vs.vehicle.vehicleId=:vehicleId")
    Optional<VehicleStatus> findByVehicleId(@Param("vehicleId") Long vehicleId);
}
