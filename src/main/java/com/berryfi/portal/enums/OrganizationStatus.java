package com.berryfi.portal.enums;

/**
 * Enumeration for organization status.
 */
public enum OrganizationStatus {
    ACTIVE("active"),
    SUSPENDED("suspended"),
    DELETED("deleted");

    private final String value;

    OrganizationStatus(String value) {
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