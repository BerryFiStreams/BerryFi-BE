package com.berryfi.portal.enums;

/**
 * Enumeration for credit status.
 */
public enum CreditStatus {
    ACTIVE("active"),
    SUSPENDED("suspended"),
    EXHAUSTED("exhausted"),
    EXPIRED("expired"),
    CANCELLED("cancelled");

    private final String value;

    CreditStatus(String value) {
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