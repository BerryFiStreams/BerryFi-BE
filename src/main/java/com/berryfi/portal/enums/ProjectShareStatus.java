package com.berryfi.portal.enums;

/**
 * Enumeration for project share status.
 */
public enum ProjectShareStatus {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    REVOKED("revoked"),
    EXPIRED("expired");

    private final String value;

    ProjectShareStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}