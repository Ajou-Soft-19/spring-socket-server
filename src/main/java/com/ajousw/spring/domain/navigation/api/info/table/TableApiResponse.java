package com.ajousw.spring.domain.navigation.api.info.table;

import com.ajousw.spring.domain.navigation.api.info.SafeNumberParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class TableApiResponse {
    protected String code;
    protected List<Coordinate> sources;
    protected List<Coordinate> destinations;
    protected List<List<Double>> durations;
    protected List<List<Double>> distances;

    @SuppressWarnings("unchecked")
    public TableApiResponse(Map<String, Object> attributes, SafeNumberParser safeNumberParser) {
        this.code = (String) attributes.get("code");

        this.sources = new ArrayList<>();
        List<Map<String, Object>> sourcesList = (List<Map<String, Object>>) attributes.get("sources");
        if (sourcesList != null) {
            for (Map<String, Object> source : sourcesList) {
                List<Double> location = safeNumberParser.parseListSafely(source.get("location"));
                this.sources.add(new Coordinate(location));
            }
        }

        this.destinations = new ArrayList<>();
        List<Map<String, Object>> destinationsList = (List<Map<String, Object>>) attributes.get("destinations");
        if (destinationsList != null) {
            for (Map<String, Object> destination : destinationsList) {
                List<Double> location = safeNumberParser.parseListSafely(destination.get("location"));
                this.destinations.add(new Coordinate(location));
            }
        }

        this.durations = safeNumberParser.parseNestedListSafely(attributes.get("durations"));
        this.distances = safeNumberParser.parseNestedListSafely(attributes.get("distances"));
    }
}
