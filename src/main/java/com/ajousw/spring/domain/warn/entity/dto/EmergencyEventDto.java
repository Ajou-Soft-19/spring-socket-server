package com.ajousw.spring.domain.warn.entity.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmergencyEventDto {

    private Long emergencyEventId;

    private Long navigationPathId;

    private String issuerEmail;

    private Boolean isActive;

    private LocalDateTime createdDate;

    private LocalDateTime endedDate;

    private List<WarnRecordDto> warnRecordDtos;

    @Builder
    public EmergencyEventDto(Long emergencyEventId, Long navigationPathId, String issuerEmail, Boolean isActive,
                             LocalDateTime createdDate, LocalDateTime endedDate, List<WarnRecordDto> warnRecordDtos) {
        this.emergencyEventId = emergencyEventId;
        this.navigationPathId = navigationPathId;
        this.issuerEmail = issuerEmail;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.endedDate = endedDate;
        this.warnRecordDtos = warnRecordDtos;
    }
}
