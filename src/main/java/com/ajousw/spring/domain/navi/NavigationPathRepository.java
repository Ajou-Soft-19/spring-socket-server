package com.ajousw.spring.domain.navi;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NavigationPathRepository extends JpaRepository<NavigationPath, Long> {

    @Query("select np from NavigationPath np where np.naviPathId=:naviPathId")
    Optional<NavigationPath> findNavigationPathByNaviPathId(@Param("naviPathId") Long naviPathId);
}
