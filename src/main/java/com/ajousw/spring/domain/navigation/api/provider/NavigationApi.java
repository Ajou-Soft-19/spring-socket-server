package com.ajousw.spring.domain.navigation.api.provider;

import java.util.Map;
import org.springframework.http.ResponseEntity;

public interface NavigationApi {

    ResponseEntity<String> getDistanceDurationTableInfo(Map<String, Object> params);

    ResponseEntity<String> getMapMatchResult(Map<String, Object> params);

}
