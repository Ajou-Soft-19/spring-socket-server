package com.ajousw.spring.domain.member.enums;

public enum Role {
    ROLE_USER("ROLE_USER"),
    ROLE_EMERGENCY_VEHICLE("ROLE_EMERGENCY_VEHICLE"),
    ROLE_ANONYMOUS("ROLE_ANONYMOUS"),
    ROLE_ADMIN("ROLE_ADMIN");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
