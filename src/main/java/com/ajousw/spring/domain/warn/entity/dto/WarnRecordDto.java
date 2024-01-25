package com.ajousw.spring.domain.warn.entity.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarnRecordDto {

    private Long checkPointId;

    private List<Long> targetVehicles;

}
