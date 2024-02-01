package com.ajousw.spring.domain.navigation.entity.repository;

import com.ajousw.spring.domain.navigation.entity.PathPoint;
import com.ajousw.spring.domain.navigation.entity.PointId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PathPointRepository extends JpaRepository<PathPoint, PointId> {

    @Query("select p from PathPoint p where p.navigationPath.naviPathId=:navigationPathId")
    List<PathPoint> getPathPointsByNavigationPathId(@Param("navigationPathId") Long navigationPathId);

}
