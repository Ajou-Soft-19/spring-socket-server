package com.ajousw.spring.domain.vehicle.entity;

public enum VehicleType {
    LIGHTWEIGHT_CAR("LIGHTWEIGHT_CAR", "1종 경형자동차"),
    SMALL_CAR("SMALL_CAR", "1종 소형차"),
    MEDIUM_CAR("MEDIUM_CAR", "2종 중형차"),
    LARGE_CAR("LARGE_CAR", "3종 대형차"),
    LARGE_TRUCK("LARGE_TRUCK", "4종 대형화물차"),
    SPECIAL_TRUCK("SPECIAL_TRUCK", "5종 특수화물차");

    private final String messageEn;
    private final String messageKr;

    VehicleType(String messageEn, String messageKr) {
        this.messageEn = messageEn;
        this.messageKr = messageKr;
    }

    public String getMessageEn() {
        return messageEn;
    }

    public String getMessageKr() {
        return messageKr;
    }
}
