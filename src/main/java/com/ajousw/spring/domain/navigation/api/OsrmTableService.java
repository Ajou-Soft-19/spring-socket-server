package com.ajousw.spring.domain.navigation.api;

import com.ajousw.spring.domain.navigation.api.info.table.TableApiResponse;
import com.ajousw.spring.domain.navigation.api.provider.NavigationProvider;
import com.ajousw.spring.domain.navigation.api.provider.factory.Provider;
import com.ajousw.spring.domain.navigation.dto.TableQueryResultDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OsrmTableService {

    private final NavigationProvider pathProvider;

    public List<TableQueryResultDto> getTableOfMultiDestDistanceAndDurationWithHeading(String source,
                                                                                       List<String> destinations,
                                                                                       List<Double> directions) {
        TableApiResponse tableQueryResult = pathProvider.getTableQueryResult(Provider.OSRM,
                createParams(List.of(source), destinations, directions));

        return createMultiDestTableQueryResultDto(source, destinations, tableQueryResult.getDistances(),
                tableQueryResult.getDurations());
    }

    public List<TableQueryResultDto> createMultiDestTableQueryResultDto(String source, List<String> destinations,
                                                                        List<List<Double>> distanceList,
                                                                        List<List<Double>> durationList) {
        List<TableQueryResultDto> tableQueryResultDtos = new ArrayList<>();
        List<Double> distances = distanceList.get(0);
        List<Double> durations = durationList.get(0);
        for (int i = 0; i < destinations.size(); i++) {
            Double distance = distances.get(i);
            Double duration = durations.get(i);
            tableQueryResultDtos.add(new TableQueryResultDto(i, source, destinations.get(i), duration, distance));
        }

        return tableQueryResultDtos;
    }

    public Map<String, Object> createParams(List<String> sources, List<String> destinations, List<Double> directions) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("sources", sources);
        params.put("destinations", destinations);
        params.put("directions", directions);
        return params;
    }
}
