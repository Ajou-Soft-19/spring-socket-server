package com.ajousw.spring.web.controller;

import com.ajousw.spring.domain.member.UserPrinciple;
import com.ajousw.spring.domain.vehicle.entity.VehicleStatus;
import com.ajousw.spring.domain.vehicle.entity.VehicleStatusRepository;
import com.ajousw.spring.socket.handler.LocationSocketHandler;
import com.ajousw.spring.web.controller.json.ApiResponseJson;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SessionBroadcastController {
    private final LocationSocketHandler locationSocketHandler;
    private final VehicleStatusRepository vehicleStatusRepository;


    @PostMapping("/api/broadcast")
    public ApiResponseJson testBroadCast(@RequestBody Map<String, Object> body,
                                         @AuthenticationPrincipal UserPrinciple userPrinciple) {
        Set<String> sessionSet = vehicleStatusRepository.findAll().stream().map(VehicleStatus::getVehicleStatusId)
                .collect(Collectors.toSet());

        log.info("json {} ", body);

        locationSocketHandler.broadcastToTargetSession(sessionSet, body);

        return new ApiResponseJson(HttpStatus.OK, "OK");
    }
}
