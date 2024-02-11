package com.ajousw.spring.domain.vehicle.record;

import com.ajousw.spring.domain.util.CoordinateUtil;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.Queue;
import lombok.Getter;

/**
 * 20m 이상 위치가 차이나거나, timeInterval이 30초 이상이면 GPS 기록을 저장
 */
@Getter
public class GPSRecorder {
    private LocationData lastLocation;
    private final Queue<LocationData> locations = new LinkedList<>();
    private final double distanceInterval = 10;
    private final int maxSize = 10;
    private final long timeInterval = 30 * 1000;

    public void record(LocationData location) {
        if (lastLocation == null) {
            lastLocation = location;
            locations.add(location);
            return;
        }

        double distance = CoordinateUtil.calculateDistance(lastLocation.getLatitude(), lastLocation.getLongitude(),
                location.getLatitude(),
                location.getLongitude());

        long timeDifference = ChronoUnit.SECONDS.between(lastLocation.getTimestamp(), location.getTimestamp());

        if (distance >= distanceInterval || timeDifference >= timeInterval) {
            if (locations.size() == maxSize) {
                locations.poll();
            }
            lastLocation = location;
            locations.add(location);
        }
    }

    public int currentSize() {
        return locations.size();
    }
}
