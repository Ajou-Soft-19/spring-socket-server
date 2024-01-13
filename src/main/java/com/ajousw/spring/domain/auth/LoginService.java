package com.ajousw.spring.domain.auth;

import com.ajousw.spring.domain.member.Member;
import com.ajousw.spring.domain.member.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
    private final MemberJpaRepository memberJpaRepository;

    public Member getMember(String email) {
        return memberJpaRepository.findByEmail(email).orElseThrow();
    }
}
