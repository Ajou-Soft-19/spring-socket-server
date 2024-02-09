package com.ajousw.spring.socket.handler.message.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurrentPointUpdateDto {

    @NotNull
    private Long naviPathId;

    @Min(0)
    private Long currentPathPoint;

    private Long emergencyEventId;

    private String email;

    private Double currentLongitude;

    private Double currentLatitude;

}
