package com.ajousw.spring.domain.vehicle.entity.init;

import com.ajousw.spring.domain.vehicle.entity.repository.VehicleStatusRepository;
import com.ajousw.spring.socket.handler.EmergencySocketHandler;
import com.ajousw.spring.socket.handler.LocationSocketHandler;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleStatusCleaner {
    private final VehicleStatusRepository vehicleStatusRepository;
    private final EmergencySocketHandler emergencySocketHandler;
    private final LocationSocketHandler locationSocketHandler;

    @Transactional
    @Scheduled(fixedDelay = 20 * 60 * 1000)
    public void removeUnUpdatedVehicle() {
        LocalDateTime twentyMinutesAgo = LocalDateTime.now().minusMinutes(20);
        Set<String> targetStatus = vehicleStatusRepository.findByLastUpdateTimeBefore(twentyMinutesAgo);
        log.info("[SCHEDULER {}] Deleting {} expired vehicle status", LocalDateTime.now(), targetStatus.size());
        emergencySocketHandler.disconnectTargetSessions(targetStatus);
        locationSocketHandler.disconnectTargetSessions(targetStatus);
    }
}
