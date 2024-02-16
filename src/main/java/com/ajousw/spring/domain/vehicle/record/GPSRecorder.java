package com.ajousw.spring.domain.vehicle.record;

import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.Queue;
import lombok.Getter;

/**
 * 10m 이상 위치가 차이나거나, timeInterval이 30초 이상이면 GPS 기록을 저장
 */
@Getter
public class GPSRecorder {
    private final String sessionId;
    private LocationData lastLocation;
    private final Queue<LocationData> locationQueue = new LinkedList<>();
    private int failCount = 0;

    public GPSRecorder(String sessionId) {
        this.sessionId = sessionId;
    }

    public void record(LocationData location) {
        if (lastLocation != null) {
            long timeDifference = ChronoUnit.SECONDS.between(lastLocation.getTimestamp(), location.getTimestamp());

            if (timeDifference >= RecordStatics.MAX_TIME_INTERVAL) {
                locationQueue.clear();
            }

            if (locationQueue.size() == RecordStatics.MAX_SIZE) {
                locationQueue.poll();
            }
        }

        locationQueue.add(location);
        lastLocation = location;
    }

    public void clear() {
        lastLocation = null;
        failCount = 0;
        locationQueue.clear();
    }

    public int currentSize() {
        return locationQueue.size();
    }

    public void addFailCount() {
        this.failCount++;
    }

    public void resetFailCount() {
        this.failCount = 0;
    }
}
