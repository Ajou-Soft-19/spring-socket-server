package com.ajousw.spring.domain.vehicle.entity.init;

import com.ajousw.spring.domain.vehicle.entity.repository.VehicleStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 예상치 못한 오류로 서버가 종료될 경우 저장된 VehicleStatus 전체 삭제
 */
@Component
@RequiredArgsConstructor
public class VehicleStatusInitializer implements CommandLineRunner {
    private final VehicleStatusRepository vehicleStatusRepository;

    @Override
    public void run(String... args) throws Exception {
        vehicleStatusRepository.deleteAll();
    }
}
