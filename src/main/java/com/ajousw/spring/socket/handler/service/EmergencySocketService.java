package com.ajousw.spring.socket.handler.service;

import com.ajousw.spring.domain.vehicle.EmergencyVehicleStatusService;
import com.ajousw.spring.domain.vehicle.record.GPSRecorder;
import com.ajousw.spring.domain.vehicle.record.LocationData;
import com.ajousw.spring.socket.exception.StatusNotInitialized;
import com.ajousw.spring.socket.handler.message.RequestType;
import com.ajousw.spring.socket.handler.message.SocketRequest;
import com.ajousw.spring.socket.handler.message.SocketResponse;
import com.ajousw.spring.socket.handler.message.convert.SocketMessageConverter;
import com.ajousw.spring.socket.handler.message.dto.VehicleStatusUpdateDto;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmergencySocketService {
    private final EmergencyVehicleStatusService vehicleStatusService;
    private final SocketMessageConverter messageConverter;

    public SocketResponse handleSocketRequest(SocketRequest socketRequest, WebSocketSession webSocketSession) {
        RequestType requestType = socketRequest.getRequestType();
        String sessionId = webSocketSession.getId();

        log.info("<{}> request type [{}]", sessionId.substring(0, 13), requestType);
        Map<String, Object> attributes = webSocketSession.getAttributes();
        Map<String, Object> requestData = socketRequest.getData();

        SocketResponse socketResponse = null;
        try {
            switch (requestType) {
                case INIT -> socketResponse = handleInit(requestData, attributes, sessionId);
                case UPDATE -> socketResponse = handleUpdate(requestData, attributes, sessionId);
                default -> throw new IllegalArgumentException("No matching request type");
            }
        } catch (IllegalArgumentException | StatusNotInitialized e) {
            return new SocketResponse(420, Map.of("errMsg", e.getMessage()));
        } catch (NullPointerException e) {
            return new SocketResponse(420, Map.of("errMsg", "잘못된 요청입니다."));
        } catch (Exception e) {
            log.error("<{}>", sessionId.substring(0, 13), e);
            return new SocketResponse(500, Map.of("errMsg", "Error While Handling Request"));
        }

        return socketResponse;
    }

    private SocketResponse handleInit(Map<String, Object> requestData, Map<String, Object> attributes,
                                      String sessionId) {
        String email = (String) attributes.get("email");
        Long vehicleId = messageConverter.getSafeValueFromMap(requestData, "vehicleId", Long.class);
        String vehicleStatusId = vehicleStatusService.resetAndCreateVehicleStatus(sessionId, email, vehicleId);

        attributes.put("vehicleId", vehicleId);
        attributes.put("vehicleStatusId", vehicleStatusId);
        attributes.put("gpsRecorder", new GPSRecorder(sessionId));

        return new SocketResponse(Map.of("vehicleStatusId", vehicleStatusId));
    }

    private SocketResponse handleUpdate(Map<String, Object> requestData, Map<String, Object> attributes,
                                        String sessionId) {
        Long vehicleId = (Long) attributes.get("vehicleId");
        checkInitialized(vehicleId);

        VehicleStatusUpdateDto updateDto = createUpdateDto(requestData);
        GPSRecorder gpsRecorder = (GPSRecorder) attributes.get("gpsRecorder");

        LocationData matchedLocation = vehicleStatusService.updateEmergencyVehicleStatus(sessionId, vehicleId,
                updateDto, gpsRecorder);

        return new SocketResponse(Map.of("location", matchedLocation));
    }

    private VehicleStatusUpdateDto createUpdateDto(Map<String, Object> requestData) {
        VehicleStatusUpdateDto updateDto = new VehicleStatusUpdateDto();
        updateDto.setIsUsingNavi(messageConverter.getSafeValueFromMap(requestData, "isUsingNavi", Boolean.class));
        updateDto.setLongitude(messageConverter.getSafeValueFromMap(requestData, "longitude", Double.class));
        updateDto.setLatitude(messageConverter.getSafeValueFromMap(requestData, "latitude", Double.class));
        updateDto.setMeterPerSec(messageConverter.getSafeValueFromMap(requestData, "meterPerSec", Double.class));
        updateDto.setDirection(messageConverter.getSafeValueFromMap(requestData, "direction", Double.class));
        updateDto.setLocalDateTime(messageConverter.parseToLocalDateTime(
                messageConverter.getSafeValueFromMap(requestData, "timestamp", String.class)));
        processEmergencyVehicle(requestData, updateDto);
        return updateDto;
    }

    private void processEmergencyVehicle(Map<String, Object> data, VehicleStatusUpdateDto updateDto) {
        updateDto.setOnEmergencyEvent(messageConverter.getSafeValueFromMap(data, "onEmergencyEvent", Boolean.class));

        if (updateDto.getIsUsingNavi() && updateDto.getOnEmergencyEvent()) {
            updateDto.setEmergencyEventId(messageConverter.getSafeValueFromMap(data, "emergencyEventId", Long.class));
        }
    }

    public void deleteStatus(Map<String, Object> attributes) {
        Long vehicleId = (Long) attributes.get("vehicleId");
        if (vehicleId == null) {
            return;
        }
        vehicleStatusService.deleteVehicleStatus(vehicleId);
        attributes.clear();
    }

    private void checkInitialized(Long vehicleId) {
        if (vehicleId == null) {
            throw new StatusNotInitialized("INIT first");
        }
    }
}
