package com.ajousw.spring.domain.navi;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PathPointRepository extends JpaRepository<PathPoint, UUID> {

}
