package com.ajousw.spring.domain.vehicle;

import com.ajousw.spring.domain.vehicle.entity.repository.VehicleStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleStatusCleaner {
    private final VehicleStatusRepository vehicleStatusRepository;

    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void removeUnUpdatedVehicle() {
        log.info("[SCHEDULER] Deleting un updated vehicle status");
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        vehicleStatusRepository.deleteByLastUpdateTimeBefore(tenMinutesAgo);
    }

}
