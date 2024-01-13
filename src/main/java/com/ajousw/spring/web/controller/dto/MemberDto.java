package com.ajousw.spring.web.controller.dto;

import com.ajousw.spring.domain.member.enums.LoginType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDto {
    private String email;

    private String username;

    private LoginType loginType;

    private LocalDateTime createdDate;
}
