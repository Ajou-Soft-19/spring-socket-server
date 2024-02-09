package com.ajousw.spring.domain.warn.entity.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarnRecordDto {

    private Long checkPointId;

    private String sessionId;

    private Double longitude;

    private Double latitude;

    private Double meterPerSec;

    private Double direction;

    private Boolean usingNavi;
}