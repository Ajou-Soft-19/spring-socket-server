package com.ajousw.spring.web.controller;

import com.ajousw.spring.domain.auth.LoginService;
import com.ajousw.spring.domain.member.Member;
import com.ajousw.spring.domain.member.UserPrinciple;
import com.ajousw.spring.web.controller.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {

    private final LoginService memberService;

    @GetMapping("/")
    public MemberDto home(@AuthenticationPrincipal UserPrinciple user) {
        Member member = memberService.getMember(user.getEmail());
        return new MemberDto(member.getEmail(), member.getUsername(), member.getLoginType(), member.getCreatedDate());
    }

}
