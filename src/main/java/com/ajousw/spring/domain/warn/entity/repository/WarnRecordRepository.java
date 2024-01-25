package com.ajousw.spring.domain.warn.entity.repository;

import com.ajousw.spring.domain.warn.entity.WarnRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarnRecordRepository extends JpaRepository<WarnRecord, Long> {
}
