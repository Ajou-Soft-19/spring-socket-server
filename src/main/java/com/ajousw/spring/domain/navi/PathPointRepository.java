package com.ajousw.spring.domain.navi;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PathPointRepository extends JpaRepository<PathPoint, UUID> {

    @Query("select p from PathPoint p where p.navigationPath.naviPathId=:navigationPathId")
    List<PathPoint> getPathPointsByNavigationPathId(@Param("navigationPathId") Long navigationPathId);

}
