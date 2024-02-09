package com.ajousw.spring.domain.navigation.api.info;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SafeNumberParser {
    // 중첩된 리스트를 안전하게 파싱하는 메서드
    public List<List<Double>> parseNestedListSafely(Object rawList) {
        if (!(rawList instanceof List)) {
            throw new IllegalArgumentException("Expected a list of lists");
        }
        return ((List<?>) rawList).stream()
                .map(this::parseListSafely)
                .collect(Collectors.toList());
    }

    // 리스트 내의 숫자를 안전하게 파싱하는 메서드
    public List<Double> parseListSafely(Object rawList) {
        if (!(rawList instanceof List)) {
            throw new IllegalArgumentException("Expected a list of numbers");
        }
        return ((List<?>) rawList).stream()
                .map(this::convertToDoubleSafely)
                .collect(Collectors.toList());
    }

    // 객체를 Double로 변환하는 메서드
    public Double convertToDoubleSafely(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Value is not a parseable number");
            }
        } else {
            throw new IllegalArgumentException("Value is not a number");
        }
    }
}
