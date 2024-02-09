package com.ajousw.spring.domain.navigation.api;


import com.ajousw.spring.domain.navigation.api.info.SafeNumberParser;
import com.ajousw.spring.domain.navigation.api.info.table.TableApiResponse;
import com.ajousw.spring.domain.navigation.api.provider.NavigationApi;
import com.ajousw.spring.domain.navigation.api.provider.Provider;
import com.ajousw.spring.domain.navigation.api.provider.impl.OsrmNavigationApi;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NavigationApiFactory {
    private final OsrmNavigationApi osrmNavigationApi;
    private final SafeNumberParser safeNumberParser;

    public NavigationApi getNavigationApi(Provider provider) {
        return switch (provider) {
            case OSRM -> osrmNavigationApi;
            default -> throw new IllegalArgumentException("Invalid provider type");
        };
    }

    public TableApiResponse parseTableApiResponse(Provider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case OSRM -> new TableApiResponse(attributes, safeNumberParser);
            default -> throw new IllegalArgumentException("Invalid provider type");
        };
    }
}
