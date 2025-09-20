package com.berryfi.portal.enums;

/**
 * Enumeration for project share types.
 */
public enum ShareType {
    ONE_TIME("one_time"),
    RECURRING("recurring"),
    READ_ONLY("read_only"),
    READ_WRITE("read_write"),
    FULL_ACCESS("full_access");

    private final String value;

    ShareType(String value) {
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