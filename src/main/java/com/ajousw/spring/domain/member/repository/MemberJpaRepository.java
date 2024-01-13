package com.ajousw.spring.domain.member.repository;

import com.ajousw.spring.domain.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    @Query("select m.id from Member m where m.email=:email")
    Optional<Long> findMemberIdByEmail(@Param("email") String email);

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

}
