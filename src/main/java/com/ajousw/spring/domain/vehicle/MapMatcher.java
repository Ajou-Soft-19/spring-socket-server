package com.ajousw.spring.domain.vehicle;

import com.ajousw.spring.domain.navigation.api.info.table.Coordinate;
import com.ajousw.spring.domain.navigation.api.info.table.MapMatchApiResponse;
import com.ajousw.spring.domain.navigation.api.provider.NavigationProvider;
import com.ajousw.spring.domain.navigation.api.provider.Provider;
import com.ajousw.spring.domain.vehicle.record.GPSRecorder;
import com.ajousw.spring.domain.vehicle.record.LocationData;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional(noRollbackFor = Exception.class)
@RequiredArgsConstructor
public class MapMatcher {
    private final NavigationProvider navigationProvider;

    /**
     * 맵 매칭을 통해 좌표를 수정하고, GPSRecorder에 위치 정보를 저장
     */
    public LocationData requestMapMatchAndRecord(GPSRecorder gpsRecorder, LocationData currentLocation) {
        if (gpsRecorder.currentSize() < 4) {
            addCurrentLocationToRecorder(gpsRecorder, currentLocation);
            return currentLocation;
        }

        LocationData location = null;
        try {
            Map<String, Object> params = setParams(gpsRecorder, currentLocation);
            MapMatchApiResponse queryResult = navigationProvider.getMapMatchQueryResult(Provider.OSRM, params);
            log.info("confidence {}", queryResult.getConfidence());
            location = returnMapMatchedLocation(queryResult, currentLocation);
        } catch (Exception e) {
            log.error("error", e);
            location = currentLocation;
        } finally {
            addCurrentLocationToRecorder(gpsRecorder, location);
        }

        return location;
    }

    private void addCurrentLocationToRecorder(GPSRecorder gpsRecorder, LocationData currentLocation) {
        gpsRecorder.record(currentLocation);
    }

    private LocationData returnMapMatchedLocation(MapMatchApiResponse queryResult, LocationData currentLocation) {
        Coordinate lastCoordinate = queryResult.getLastCoordinate();
        double lon = lastCoordinate.getLongitude();
        double lat = lastCoordinate.getLatitude();
        String locationName = queryResult.getCurrentLocationName();

        return new LocationData(lat, lon, currentLocation.getDirection(), currentLocation.getTimestamp(), locationName);
    }

    private Map<String, Object> setParams(GPSRecorder gpsRecorder, LocationData currentLocation) {
        Queue<LocationData> locations = gpsRecorder.getLocations();
        List<String> coordinates = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>();
        List<Double> bearings = new ArrayList<>();

        for (LocationData location : locations) {
            coordinates.add(location.getLongitude() + "," + location.getLatitude());
            timestamps.add(location.getTimestamp().toEpochSecond(ZoneOffset.UTC));
            bearings.add(location.getDirection());
        }

        coordinates.add(currentLocation.getLongitude() + "," + currentLocation.getLatitude());
        timestamps.add(currentLocation.getTimestamp().toEpochSecond(ZoneOffset.UTC));
        bearings.add(currentLocation.getDirection());

        Map<String, Object> params = new HashMap<>();
        params.put("coordinates", coordinates);
        params.put("timestamps", timestamps);
        params.put("bearings", bearings);
        return params;
    }
}
