package com.ajousw.spring.socket;

import com.ajousw.spring.domain.vehicle.EmergencyVehicleStatusService;
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
public class EmergencySocketController {
    private final EmergencyVehicleStatusService vehicleStatusService;
    private final SocketMessageConverter messageConverter;

    public SocketResponse handleSocketRequest(SocketRequest socketRequest, WebSocketSession webSocketSession) {
        RequestType requestType = socketRequest.getRequestType();
        log.info("<{}> request type [{}]", webSocketSession.getId(), requestType);
        Map<String, Object> attributes = webSocketSession.getAttributes();

        return handleRequest(socketRequest, requestType, attributes, webSocketSession.getId());
    }

    private SocketResponse handleRequest(SocketRequest socketRequest, RequestType requestType,
                                         Map<String, Object> attributes, String sessionId) {
        SocketResponse socketResponse = null;
        Map<String, Object> data = socketRequest.getData();
        try {
            switch (requestType) {
                case INIT -> socketResponse = init(data, attributes, sessionId);
                case UPDATE -> socketResponse = update(data, attributes);
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
                                String sessionId) {
        String email = (String) attributes.get("email");
        Long vehicleId = messageConverter.getSafeValueFromMap(data, "vehicleId", Long.class);
        String vehicleStatusId = vehicleStatusService.resetAndCreateVehicleStatus(sessionId, email, vehicleId);
        attributes.put("vehicleId", vehicleId);
        attributes.put("vehicleStatusId", vehicleStatusId);
        return new SocketResponse(Map.of("vehicleStatusId", vehicleStatusId));
    }

    private SocketResponse update(Map<String, Object> data, Map<String, Object> attributes) {
        Long vehicleId = (Long) attributes.get("vehicleId");
        String email = (String) attributes.get("email");
        checkInitialized(vehicleId);

        VehicleStatusUpdateDto updateDto = createUpdateDto(data);
        processEmergencyVehicle(data, updateDto);

        String message = vehicleStatusService.updateEmergencyVehicleStatus(email, vehicleId, updateDto)
                .orElse("OK");
        return new SocketResponse(Map.of("msg", message));
    }

    private VehicleStatusUpdateDto createUpdateDto(Map<String, Object> data) {
        VehicleStatusUpdateDto updateDto = new VehicleStatusUpdateDto();
        updateDto.setIsUsingNavi(messageConverter.getSafeValueFromMap(data, "isUsingNavi", Boolean.class));
        updateDto.setLongitude(messageConverter.getSafeValueFromMap(data, "longitude", Double.class));
        updateDto.setLatitude(messageConverter.getSafeValueFromMap(data, "latitude", Double.class));
        updateDto.setMeterPerSec(messageConverter.getSafeValueFromMap(data, "meterPerSec", Double.class));
        updateDto.setDirection(messageConverter.getSafeValueFromMap(data, "direction", Double.class));
        updateDto.setLocalDateTime(messageConverter.parseToLocalDateTime(
                messageConverter.getSafeValueFromMap(data, "timestamp", String.class)));
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
        attributes.remove("vehicleId");
        attributes.remove("vehicleStatusId");
    }

    private void checkInitialized(Long vehicleId) {
        if (vehicleId == null) {
            throw new StatusNotInitialized("INIT first");
        }
    }
}
