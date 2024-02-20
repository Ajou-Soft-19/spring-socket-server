package com.ajousw.spring.domain.vehicle.entity.init;

import com.ajousw.spring.domain.vehicle.entity.repository.VehicleStatusRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleStatusCleaner {
    private final VehicleStatusRepository vehicleStatusRepository;

    @Transactional
    @Scheduled(fixedDelay = 1200000)
    public void removeUnUpdatedVehicle() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(20);
        long deletedCount = vehicleStatusRepository.deleteByLastUpdateTimeBefore(tenMinutesAgo);
        log.info("[SCHEDULER {}] Deleting {} expired vehicle status", LocalDateTime.now(), deletedCount);
    }
}
