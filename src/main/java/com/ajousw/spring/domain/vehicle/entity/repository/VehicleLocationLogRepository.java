package com.ajousw.spring.domain.vehicle.entity.repository;

import com.ajousw.spring.domain.vehicle.entity.VehicleLocationLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleLocationLogRepository extends JpaRepository<VehicleLocationLog, Long> {

    List<VehicleLocationLog> findVehicleLocationLogsByVehicleIdOrderByLastUpdateTimeAsc(Long vehicleId);
}
