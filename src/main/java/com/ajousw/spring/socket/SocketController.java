package com.ajousw.spring.socket;

import com.ajousw.spring.domain.vehicle.VehicleStatusService;
import com.ajousw.spring.socket.exception.StatusNotInitialized;
import com.ajousw.spring.socket.handler.message.RequestType;
import com.ajousw.spring.socket.handler.message.SocketRequest;
import com.ajousw.spring.socket.handler.message.SocketResponse;
import com.ajousw.spring.socket.handler.message.dto.VehicleStatusUpdateDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocketController {
    private final VehicleStatusService vehicleStatusService;

    public SocketResponse handleSocketRequest(SocketRequest socketRequest, WebSocketSession webSocketSession,
                                              boolean isEmergencyVehicle) {
        RequestType requestType = socketRequest.getRequestType();
        log.info("<{}> request type [{}]", webSocketSession.getId(), requestType);
        Map<String, Object> attributes = webSocketSession.getAttributes();

        return handleRequest(socketRequest, requestType, attributes, webSocketSession.getId(),
                isEmergencyVehicle);
    }

    private SocketResponse handleRequest(SocketRequest socketRequest, RequestType requestType,
                                         Map<String, Object> attributes, String sessionId,
                                         boolean isEmergencyVehicle) {
        SocketResponse socketResponse = null;
        Map<String, Object> data = socketRequest.getData();
        try {
            switch (requestType) {
                case INIT -> socketResponse = init(data, attributes, sessionId, isEmergencyVehicle);
                case UPDATE -> socketResponse = update(data, attributes, isEmergencyVehicle);
                default -> throw new IllegalArgumentException("No matching request type");
            }
        } catch (IllegalArgumentException | StatusNotInitialized e) {
            return new SocketResponse(420, Map.of("errMsg", e.getMessage()));
        } catch (NullPointerException e) {
            return new SocketResponse(420, Map.of("errMsg", "잘못된 요청입니다."));
        } catch (Exception e) {
            log.error("", e);
            return new SocketResponse(500, Map.of("errMsg", "Error While Handling Request"));
        }

        return socketResponse;
    }

    private SocketResponse init(Map<String, Object> data, Map<String, Object> attributes,
                                String sessionId, boolean isEmergencyVehicle) {
        String email = (String) attributes.get("email");
        Long vehicleId = getSafeValueFromMap(data, "vehicleId", Long.class);
        String vehicleStatusId = vehicleStatusService.resetAndCreateVehicleStatus(sessionId, email, vehicleId,
                isEmergencyVehicle);
        attributes.put("vehicleId", vehicleId);
        attributes.put("vehicleStatusId", vehicleStatusId);
        return new SocketResponse(Map.of("vehicleStatusId", vehicleStatusId));
    }

    private SocketResponse update(Map<String, Object> data, Map<String, Object> attributes,
                                  boolean isEmergencyVehicle) {
        Long vehicleId = (Long) attributes.get("vehicleId");
        String email = (String) attributes.get("email");
        checkInitialized(vehicleId);

        VehicleStatusUpdateDto updateDto = new VehicleStatusUpdateDto();
        updateDto.setIsUsingNavi(getSafeValueFromMap(data, "isUsingNavi", Boolean.class));
        updateDto.setLongitude(getSafeValueFromMap(data, "longitude", Double.class));
        updateDto.setLatitude(getSafeValueFromMap(data, "latitude", Double.class));
        updateDto.setMeterPerSec(getSafeValueFromMap(data, "meterPerSec", Double.class));
        updateDto.setDirection(getSafeValueFromMap(data, "direction", Double.class));
        updateDto.setLocalDateTime(parseToLocalDateTime(getSafeValueFromMap(data, "timestamp", String.class)));

        if (!isEmergencyVehicle) {
            vehicleStatusService.updateVehicleStatus(vehicleId, updateDto);
            return new SocketResponse(Map.of("msg", "OK"));
        }

        updateDto.setOnEmergencyEvent(getSafeValueFromMap(data, "onEmergencyEvent", Boolean.class));

        if (updateDto.getIsUsingNavi() && updateDto.getOnEmergencyEvent()) {
            updateDto.setNaviPathId(getSafeValueFromMap(data, "naviPathId", Long.class));
            updateDto.setEmergencyEventId(getSafeValueFromMap(data, "emergencyEventId", Long.class));
        }
        
        vehicleStatusService.updateEmergencyVehicleStatus(email, vehicleId, updateDto);

        return new SocketResponse(Map.of("msg", "OK"));
    }

    public void deleteStatus(Map<String, Object> attributes) {
        Long vehicleId = (Long) attributes.get("vehicleId");
        if (vehicleId == null) {
            return;
        }
        vehicleStatusService.deleteVehicleStatus(vehicleId);
        attributes.remove("vehicleId");
        attributes.remove("vehicleStatusId");
    }

    private void checkInitialized(Long vehicleId) {
        if (vehicleId == null) {
            throw new StatusNotInitialized("INIT first");
        }
    }

    private <T> T getSafeValueFromMap(Map<String, Object> data, String key, Class<T> type) {
        if (!data.containsKey(key)) {
            throw new IllegalArgumentException(String.format("Missing data %s", key));
        }

        Object value = data.get(key);

        if (type == Long.class && value instanceof Integer) {
            return type.cast(((Integer) value).longValue());
        }

        if (type == Double.class && value instanceof Integer) {
            return type.cast(((Integer) value).doubleValue());
        }

        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                    String.format("Wrong data type for key %s, Need %s, but was %s", key, type,
                            value.getClass()));
        }

        return type.cast(value);
    }

    private LocalDateTime parseToLocalDateTime(String timestampStr) {
        try {
            return LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid format for timestamp", e);
        }
    }


}
