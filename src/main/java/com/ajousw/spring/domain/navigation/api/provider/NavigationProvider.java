package com.ajousw.spring.domain.navigation.api.provider;

import com.ajousw.spring.domain.navigation.api.info.table.MapMatchApiResponse;
import com.ajousw.spring.domain.navigation.api.info.table.TableApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NavigationProvider {

    private ObjectMapper mapper;
    private final NavigationApiFactory navigationApiFactory;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
    }

    public TableApiResponse getTableQueryResult(Provider provider, Map<String, Object> params) {
        NavigationApi navigationApi = navigationApiFactory.getNavigationApi(provider);
        ResponseEntity<String> response = navigationApi.getDistanceDurationTableInfo(params);

        try {
            Map<String, Object> attributes = mapper.readValue(response.getBody(), Map.class);
            return navigationApiFactory.parseTableApiResponse(provider, attributes);
        } catch (JsonProcessingException | NullPointerException e) {
            throw new RuntimeException("거리,시간 계산 API 파싱 중 오류 발생", e);
        }
    }

    public MapMatchApiResponse getMapMatchQueryResult(Provider provider, Map<String, Object> params) {
        NavigationApi navigationApi = navigationApiFactory.getMapMatchApi(provider);
        ResponseEntity<String> response = navigationApi.getMapMatchResult(params);

        try {
            Map<String, Object> attributes = mapper.readValue(response.getBody(), Map.class);
            return navigationApiFactory.parseMapMatchApiResponse(provider, attributes);
        } catch (JsonProcessingException | NullPointerException e) {
            throw new RuntimeException("맵 매칭 API 파싱 중 오류 발생", e);
        }
    }

}
