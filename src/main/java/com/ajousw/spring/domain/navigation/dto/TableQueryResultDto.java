package com.ajousw.spring.domain.navigation.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TableQueryResultDto {

    private int index;

    private String source;

    private String destination;

    private Double duration;

    private Double distance;

}
