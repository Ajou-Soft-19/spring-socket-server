package com.ajousw.spring.socket.handler.message.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VehicleStatusUpdateDto {
    private Boolean isUsingNavi;
    private Long naviPathId;
    private Double longitude;
    private Double latitude;
    private Double meterPerSec;
    private Double direction;
    private LocalDateTime localDateTime;
}
