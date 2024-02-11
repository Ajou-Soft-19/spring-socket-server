package com.ajousw.spring.domain.navigation.api.info.table;

import java.util.List;
import lombok.Data;

@Data
public class Coordinate {
    private final double longitude;
    private final double latitude;

    public Coordinate(List<Double> pathInfo) {
        this.longitude = pathInfo.get(0);
        this.latitude = pathInfo.get(1);
    }
}