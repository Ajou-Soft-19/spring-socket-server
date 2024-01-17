package com.ajousw.spring.socket;

import com.ajousw.spring.domain.vehicle.VehicleStatusService;
import com.ajousw.spring.socket.exception.StatusNotInitialized;
import com.ajousw.spring.socket.handler.json.RequestType;
import com.ajousw.spring.socket.handler.json.SocketRequest;
import com.ajousw.spring.socket.handler.json.SocketResponse;
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

    public SocketResponse handleSocketRequest(SocketRequest socketRequest, WebSocketSession webSocketSession, boolean isEmergencyVehicle) {
        RequestType requestType = socketRequest.getRequestType();
        log.info("<{}> request type [{}]", webSocketSession.getId(), requestType);

        Map<String, Object> attributes = webSocketSession.getAttributes();
        String email = (String) attributes.get("email");

        if (requestType == null) {
            return new SocketResponse(420, "No Matching Request Type");
        }

        return handleRequest(socketRequest, requestType, attributes, webSocketSession.getId(), email, isEmergencyVehicle);
    }

    private SocketResponse handleRequest(SocketRequest socketRequest, RequestType requestType,
                                         Map<String, Object> attributes, String sessionId, String email, boolean isEmergencyVehicle) {
        SocketResponse socketResponse = null;
        Map<String, Object> data = socketRequest.getData();
        try {
            switch (requestType) {
                case INIT -> socketResponse = init(data, email, attributes, sessionId, isEmergencyVehicle);
                case UPDATE -> socketResponse = update(data, attributes);
            }
        } catch (IllegalArgumentException | StatusNotInitialized e) {
            return new SocketResponse(420, Map.of("errMsg", e.getMessage()));
        } catch (Exception e) {
            log.error("", e);
            return new SocketResponse(500, Map.of("errMsg", "Error While Handling Request"));
        }

        return socketResponse;
    }

    private SocketResponse init(Map<String, Object> data, String email, Map<String, Object> attributes,
                                String sessionId, boolean isEmergencyVehicle) {
        Long vehicleId = getSafeValueFromMap(data, "vehicleId", Long.class);
        String vehicleStatusId = vehicleStatusService.createVehicleStatus(sessionId, email, vehicleId, isEmergencyVehicle);
        attributes.put("vehicleId", vehicleId);
        attributes.put("vehicleStatusId", vehicleStatusId);
        return new SocketResponse(Map.of("vehicleStatusId", vehicleStatusId));
    }

    private SocketResponse update(Map<String, Object> data, Map<String, Object> attributes) {
        Long vehicleId = (Long) attributes.get("vehicleId");
        checkInitialized(vehicleId);

        Boolean isUsingNavi = getSafeValueFromMap(data, "isUsingNavi", Boolean.class);
        Double longitude = getSafeValueFromMap(data, "longitude", Double.class);
        Double latitude = getSafeValueFromMap(data, "latitude", Double.class);
        Double meterPerSec = getSafeValueFromMap(data, "meterPerSec", Double.class);
        Double direction = getSafeValueFromMap(data, "direction", Double.class);
        LocalDateTime localDateTime = parseToLocalDateTime(getSafeValueFromMap(data, "timestamp", String.class));

        vehicleStatusService.updateVehicleStatus(vehicleId, isUsingNavi, longitude, latitude, meterPerSec, direction,
                localDateTime);
        return new SocketResponse(Map.of("msg", "OK"));
    }

    public SocketResponse deleteStatus(Map<String, Object> attributes) {
        Long vehicleId = (Long) attributes.get("vehicleId");
        checkInitialized(vehicleId);
        vehicleStatusService.deleteVehicleStatus(vehicleId);
        attributes.remove("vehicleId");
        attributes.remove("vehicleStatusId");
        return new SocketResponse(Map.of("msg", "OK"));
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
