package com.ajousw.spring.web.controller;

import com.ajousw.spring.domain.vehicle.entity.repository.VehicleStatusRepository;
import com.ajousw.spring.socket.handler.LocationSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

// TODO: 이후 완전히 삭제
@Slf4j
@RestController
@RequiredArgsConstructor
public class SessionBroadcastController {
    private final LocationSocketHandler locationSocketHandler;
    private final VehicleStatusRepository vehicleStatusRepository;

//    public ApiResponseJson testBroadCast(@RequestBody Map<String, Object> body,
//                                         @AuthenticationPrincipal UserPrinciple userPrinciple) {
//        Set<String> sessionSet = vehicleStatusRepository.findAll().stream().map(VehicleStatus::getVehicleStatusId)
//                .collect(Collectors.toSet());
//
//        log.info("json {} ", body);
//
//        locationSocketHandler.broadcastToTargetSession(sessionSet, body);
//
//        return new ApiResponseJson(HttpStatus.OK, "OK");
//    }
}
