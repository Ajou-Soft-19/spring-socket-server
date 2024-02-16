package com.ajousw.spring.domain.navigation.api;

import com.ajousw.spring.domain.navigation.api.info.table.Coordinate;
import com.ajousw.spring.domain.navigation.api.info.table.MapMatchApiResponse;
import com.ajousw.spring.domain.navigation.api.provider.NavigationProvider;
import com.ajousw.spring.domain.navigation.api.provider.factory.Provider;
import com.ajousw.spring.domain.vehicle.record.GPSRecorder;
import com.ajousw.spring.domain.vehicle.record.LocationData;
import com.ajousw.spring.domain.vehicle.record.RecordStatics;
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
    private final int minRecordSize = 4;

    /**
     * 맵 매칭을 통해 좌표를 수정하고, GPSRecorder에 위치 정보를 저장
     */
    // TODO: 통신 오류와 맵매칭 실패 구분하여 처리 + Direction도 반환해주기
    public LocationData requestMapMatchAndRecord(GPSRecorder gpsRecorder, LocationData currentLocation) {
        if (gpsRecorder.currentSize() < minRecordSize) {
            addCurrentLocationToRecorder(gpsRecorder, currentLocation);
            return currentLocation;
        }

//        LocationData mapMatchedLocation = null;
        try {
            Map<String, Object> params = setParams(gpsRecorder, currentLocation);
            MapMatchApiResponse queryResult = navigationProvider.getMapMatchQueryResult(Provider.OSRM, params);
            logMapMatchedResult(gpsRecorder, queryResult);
            gpsRecorder.resetFailCount();
//            mapMatchedLocation = returnMapMatchedLocation(queryResult, currentLocation);
        } catch (Exception e) {
            handleMapMatchError(gpsRecorder, e);
//            mapMatchedLocation = currentLocation;
        } finally {
            addCurrentLocationToRecorder(gpsRecorder, currentLocation);
        }

        return currentLocation;
    }

    private void handleMapMatchError(GPSRecorder gpsRecorder, Exception e) {
        log.info("<{}> No Match result", gpsRecorder.getSessionId().substring(0, 13));
        log.info("<{}> cause {}", gpsRecorder.getSessionId().substring(0, 13), e.getMessage());
        gpsRecorder.addFailCount();
        if (gpsRecorder.currentSize() > RecordStatics.MAX_SIZE / 2
                && gpsRecorder.getFailCount() > RecordStatics.MAX_ERROR_COUNT) {
            gpsRecorder.clear();
        }
    }

    private void logMapMatchedResult(GPSRecorder gpsRecorder, MapMatchApiResponse queryResult) {
        log.info("<{}> confidence {} lat: {} lon: {}", gpsRecorder.getSessionId().substring(0, 13),
                queryResult.getConfidence(), queryResult.getLastCoordinate().getLatitude(),
                queryResult.getLastCoordinate().getLongitude());
    }

    private void addCurrentLocationToRecorder(GPSRecorder gpsRecorder, LocationData currentLocation) {
        gpsRecorder.record(currentLocation);
    }

    private LocationData returnMapMatchedLocation(MapMatchApiResponse queryResult, LocationData currentLocation) {
        Coordinate lastCoordinate = queryResult.getLastCoordinate();
        double lon = lastCoordinate.getLongitude();
        double lat = lastCoordinate.getLatitude();
        String locationName = queryResult.getCurrentLocationName();
        double confidence = queryResult.getConfidence();

        return new LocationData(lat, lon, currentLocation.getDirection(), currentLocation.getTimestamp(), locationName,
                confidence);
    }

    private Map<String, Object> setParams(GPSRecorder gpsRecorder, LocationData currentLocation) {
        Queue<LocationData> locations = gpsRecorder.getLocationQueue();
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
