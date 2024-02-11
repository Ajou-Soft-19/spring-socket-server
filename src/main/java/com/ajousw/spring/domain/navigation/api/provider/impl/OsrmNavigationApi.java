package com.ajousw.spring.domain.navigation.api.provider.impl;

import com.ajousw.spring.domain.exception.BadApiResponseException;
import com.ajousw.spring.domain.navigation.api.provider.NavigationApi;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Getter
@Component
public class OsrmNavigationApi implements NavigationApi {

    @Value("${navigation.api.osrm.table-url}")
    private String tableRequestUrl;

    @Value("${navigation.api.osrm.match-url}")
    private String mapMatchRequestUrl;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        webClient = WebClient.builder().build();
    }

    public ResponseEntity<String> getDistanceDurationTableInfo(Map<String, Object> params) {
        ResponseEntity<String> response = null;
        try {
            response = webClient.get()
                    .uri(setTableParams(tableRequestUrl, (List<String>) params.get("sources"),
                            (List<String>) params.get("destinations"), (List<Double>) params.get("directions")))
                    .retrieve()
                    .toEntity(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            if (statusCode.isError()) {
                log.error("OSRM table api {} error", e.getStatusCode(), e);
                throw new BadApiResponseException("API 서버에 오류가 발생했습니다.");
            }
        }

        return response;
    }

    public ResponseEntity<String> getMapMatchResult(Map<String, Object> params) {
        ResponseEntity<String> response = null;
        try {
            response = webClient.get()
                    .uri(setMapMatchParams(mapMatchRequestUrl, (List<String>) params.get("coordinates"),
                            (List<Long>) params.get("timestamps"), (List<Double>) params.get("bearings")))
                    .retrieve()
                    .toEntity(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            if (statusCode.isError()) {
                log.error("OSRM map match api {} error", e.getStatusCode(), e);
                throw new BadApiResponseException("API 서버에 오류가 발생했습니다.");
            }
        }

        return response;
    }

    private String setTableParams(String requestUrl, List<String> sources, List<String> destinations,
                                  List<Double> directions) {
        List<String> coordinates = new ArrayList<>();
        coordinates.addAll(sources);
        coordinates.addAll(destinations);
        String coordinatesString = String.join(";", coordinates);

        String startPointString = IntStream.rangeClosed(0, sources.size() - 1)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(";"));

        String destinationString = IntStream.rangeClosed(sources.size(), sources.size() + destinations.size() - 1)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(";"));

        String formattedRequestUrl = String.format(requestUrl, coordinatesString, startPointString, destinationString);
        if (directions == null) {
            return formattedRequestUrl;
        }

        if (sources.size() != directions.size()) {
            throw new IllegalArgumentException("Source size and Direction size are not equal");
        }

        String bearingsString = directions.stream()
                .map(direction -> direction + ",20")
                .collect(Collectors.joining(";"));

        return String.format("%s&bearings=%s", formattedRequestUrl, bearingsString);
    }

    private String setMapMatchParams(String requestUrl, List<String> coordinates, List<Long> timestamps,
                                     List<Double> bearings) {
        String coordinatesString = String.join(";", coordinates);
        String timestampsString = timestamps.stream()
                .map(Object::toString)
                .collect(Collectors.joining(";"));
        String bearingsString = bearings.stream()
                .map(direction -> direction + ",20")
                .collect(Collectors.joining(";"));

        return String.format(requestUrl, coordinatesString, timestampsString);
    }

}
