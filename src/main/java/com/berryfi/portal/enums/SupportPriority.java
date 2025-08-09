package com.berryfi.portal.enums;

/**
 * Enumeration for support request priority.
 */
public enum SupportPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String value;

    SupportPriority(String value) {
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
