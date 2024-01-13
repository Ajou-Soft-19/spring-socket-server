package com.ajousw.spring.domain.vehicle;

import com.ajousw.spring.domain.member.Member;
import com.ajousw.spring.domain.member.repository.MemberJpaRepository;
import com.ajousw.spring.domain.vehicle.entity.Vehicle;
import com.ajousw.spring.domain.vehicle.entity.VehicleRepository;
import com.ajousw.spring.domain.vehicle.entity.VehicleStatus;
import com.ajousw.spring.domain.vehicle.entity.VehicleStatusRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: 영어로 바꾸기...
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VehicleStatusService {
    private final VehicleStatusRepository vehicleStatusRepository;
    private final VehicleRepository vehicleRepository;
    private final MemberJpaRepository memberRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public String createVehicleStatus(String sessionId, String email, Long vehicleId) {
        Member member = findMemberByEmail(email);
        Vehicle vehicle = findVehicleById(vehicleId);

        if (!Objects.equals(member.getId(), vehicle.getMember().getId())) {
            throw new IllegalArgumentException("본인이 소유한 차량이 아닙니다.");
        }

        vehicleStatusRepository.deleteByVehicle(vehicle);
        vehicleStatusRepository.flush();
        VehicleStatus vehicleStatus = new VehicleStatus(sessionId, vehicle, false, null, -1, -1, LocalDateTime.now());
        vehicleStatusRepository.save(vehicleStatus);

        return vehicleStatus.getVehicleStatusId();
    }

    public void updateVehicleStatus(Long vehicleId, boolean isUsingNavi, double longitude, double latitude,
                                    double meterPerSec,
                                    double direction, LocalDateTime lastUpdateTime) {
        VehicleStatus vehicleStatus = findVehicleStatusByVehicleId(vehicleId);

        Point coordinate = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        vehicleStatus.modifyStatus(isUsingNavi, coordinate, meterPerSec, direction, lastUpdateTime);
    }

    public void deleteVehicleStatus(Long vehicleId) {
        vehicleStatusRepository.deleteByVehicleId(vehicleId);
    }

    private VehicleStatus findVehicleStatusByVehicleId(Long vehicleId) {
        return vehicleStatusRepository.findByVehicleId(vehicleId).orElseThrow(() -> {
            log.info("Vehicle Status Not Found : [{}]", vehicleId);
            return new IllegalArgumentException("Vehicle Status Not Found");
        });
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> {
            log.info("No Such member [email:{}]", email);
            return new IllegalArgumentException("No Such member");
        });
    }

    private Vehicle findVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId).orElseThrow(() -> {
            log.info("Vehicle Not Found : [{}]", vehicleId);
            return new IllegalArgumentException("Vehicle Not Found");
        });
    }

}
