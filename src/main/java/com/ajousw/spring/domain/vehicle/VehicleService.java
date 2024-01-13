package com.ajousw.spring.domain.vehicle;

import com.ajousw.spring.domain.vehicle.entity.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public void addVehicle() {

    }
}
