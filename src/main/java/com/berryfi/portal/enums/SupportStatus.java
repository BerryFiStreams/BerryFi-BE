package com.berryfi.portal.enums;

/**
 * Enumeration for support request status.
 */
public enum SupportStatus {
    OPEN("open"),
    IN_PROGRESS("in_progress"),
    RESOLVED("resolved"),
    CLOSED("closed");

    private final String value;

    SupportStatus(String value) {
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
