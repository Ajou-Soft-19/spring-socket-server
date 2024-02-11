package com.ajousw.spring.socket.handler.service;

import com.ajousw.spring.socket.handler.LocationSocketHandler;
import com.ajousw.spring.socket.handler.message.MessageType;
import com.ajousw.spring.socket.handler.message.dto.AlertEndDto;
import com.ajousw.spring.socket.handler.message.dto.AlertUpdateDto;
import jakarta.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContinuousAlertTransmitter {

    private final Map<Long, Map<String, SessionNotificationCounter>> alertInfoMap = new ConcurrentHashMap<>();
    private final Map<Long, String> licenseNumberInfo = new ConcurrentHashMap<>();
    private final LocationSocketHandler locationSocketHandler;
    private static final int ALERT_COUNT = 40;

    public void registerTransmitter(Long vehicleId, String licenseNumber) {
        if (!alertInfoMap.containsKey(vehicleId)) {
            alertInfoMap.put(vehicleId, new ConcurrentHashMap<>());
            licenseNumberInfo.put(vehicleId, licenseNumber);
        }
    }

    public void removeTransmitter(Long vehicleId) {
        Map<String, SessionNotificationCounter> sessionNotificationCounterMap = alertInfoMap.remove(vehicleId);
        String licenseNumber = licenseNumberInfo.remove(vehicleId);

        if (sessionNotificationCounterMap == null) {
            return;
        }

        sendAlertEndMessage(sessionNotificationCounterMap, licenseNumber);
    }

    @PreDestroy
    public void preDestroy() {
        alertInfoMap.forEach((vehicleId, sessionNotificationCounterMap) -> {
            String licenseNumber = licenseNumberInfo.get(vehicleId);
            sendAlertEndMessage(sessionNotificationCounterMap, licenseNumber);
        });
    }

    public void addSessions(Long vehicleId, Set<String> sessions) {
        Map<String, SessionNotificationCounter> sessionNotificationCounterMap = alertInfoMap.get(vehicleId);
        if (sessionNotificationCounterMap == null) {
            log.error("Map<String, SessionNotificationCounter> is missing");
            return;
        }
        sessions.forEach(session -> sessionNotificationCounterMap.compute(session, this::addOrResetSessionCount));
    }

    public void broadcastLocation(Long vehicleId, Double longitude, Double latitude) {
        Map<String, SessionNotificationCounter> sessionNotificationCounterMap = alertInfoMap.get(vehicleId);
        String licenseNumber = licenseNumberInfo.get(vehicleId);
        if (sessionNotificationCounterMap == null || sessionNotificationCounterMap.isEmpty()) {
            return;
        }

        AlertUpdateDto alertUpdateDto = new AlertUpdateDto(licenseNumberInfo.get(vehicleId), longitude, latitude);
        broadcastTargetSession(sessionNotificationCounterMap, alertUpdateDto, MessageType.ALERT_UPDATE);

        Set<String> removedSessionId = new HashSet<>();
        sessionNotificationCounterMap.forEach((sessionId, info) -> {
            info.reduceCount();
            if (info.isCountZero()) {
                removedSessionId.add(info.getSessionId());
                sessionNotificationCounterMap.remove(sessionId);
            }
        });

        AlertEndDto alertEndDto = new AlertEndDto(licenseNumber);
        locationSocketHandler.broadcastToTargetSession(removedSessionId, alertEndDto, MessageType.ALERT_END);
    }

    private void sendAlertEndMessage(Map<String, SessionNotificationCounter> sessionNotificationCounterMap,
                                     String licenseNumber) {
        AlertEndDto alertEndDto = new AlertEndDto(licenseNumber);
        broadcastTargetSession(sessionNotificationCounterMap, alertEndDto, MessageType.ALERT_END);
    }

    private void broadcastTargetSession(Map<String, SessionNotificationCounter> sessionNotificationCounterMap,
                                        Object object, MessageType messageType) {
        Set<String> targetSessionIds = sessionNotificationCounterMap.keySet();
        locationSocketHandler.broadcastToTargetSession(targetSessionIds, object, messageType);
    }

    private SessionNotificationCounter addOrResetSessionCount(String session, SessionNotificationCounter v) {
        if (v == null) {
            return new SessionNotificationCounter(session);
        }

        v.resetCount();
        return v;
    }

    @Getter
    public static class SessionNotificationCounter {
        private final String sessionId;
        private int alertCount;

        public SessionNotificationCounter(String sessionId) {
            this.sessionId = sessionId;
            this.alertCount = ALERT_COUNT;
        }

        public void reduceCount() {
            this.alertCount = this.alertCount - 1;
        }

        public boolean isCountZero() {
            return alertCount <= 0;
        }

        public void resetCount() {
            this.alertCount = ALERT_COUNT;
        }
    }
}
