package com.ajousw.spring.domain.navigation.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointId implements Serializable {

    private Long navigationPathId;

    private Long pointIndex;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PointId that = (PointId) o;
        return Objects.equals(navigationPathId, that.navigationPathId)
                && Objects.equals(pointIndex, that.pointIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(navigationPathId, pointIndex);
    }
}