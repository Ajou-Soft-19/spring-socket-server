package com.ajousw.spring.domain.navigation.api.info.table;

import com.ajousw.spring.domain.navigation.api.info.SafeNumberParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

// 위치는 가장 최근 위치만 반환
@Data
public class MapMatchApiResponse {
    protected String code;
    protected Double confidence;
    protected List<Coordinate> coordinates;
    protected String currentLocationName;

    @SuppressWarnings("unchecked")
    public MapMatchApiResponse(Map<String, Object> attributes, SafeNumberParser safeNumberParser) {
        this.code = (String) attributes.get("code");

        List<Map<String, Object>> matchingsList = (List<Map<String, Object>>) attributes.get("matchings");
        if (matchingsList != null && !matchingsList.isEmpty()) {
            Map<String, Object> firstMatching = matchingsList.get(0);
            this.confidence = safeNumberParser.convertToDoubleSafely(firstMatching.get("confidence"));

            this.coordinates = new ArrayList<>();
            Map<String, Object> geometry = (Map<String, Object>) firstMatching.get("geometry");
            List<List<Double>> coordinatesList = (List<List<Double>>) geometry.get("coordinates");
            if (coordinatesList != null) {
                for (List<Double> coordinate : coordinatesList) {
                    this.coordinates.add(new Coordinate(coordinate));
                }
            }
        }

        List<Map<String, Object>> tracepointsList = (List<Map<String, Object>>) attributes.get("tracepoints");
        if (tracepointsList != null && !tracepointsList.isEmpty()) {
            Map<String, Object> lastValidTracepoint = null;
            for (Map<String, Object> tracepoint : tracepointsList) {
                if (tracepoint != null && (Integer) tracepoint.get("matchings_index") == 0) {
                    lastValidTracepoint = tracepoint;
                }
            }
            if (lastValidTracepoint != null) {
                this.currentLocationName = (String) lastValidTracepoint.get("name");
            }
        }
    }

    public Coordinate getLastCoordinate() {
        if (coordinates.size() == 0) {
            return null;
        }

        return coordinates.get(coordinates.size() - 1);
    }
}
