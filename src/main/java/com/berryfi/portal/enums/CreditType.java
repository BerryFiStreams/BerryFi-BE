package com.berryfi.portal.enums;

/**
 * Enumeration for credit types.
 */
public enum CreditType {
    PURCHASED("purchased"),
    GIFTED("gifted"),
    PROMOTIONAL("promotional"),
    TRIAL("trial"),
    BONUS("bonus");

    private final String value;

    CreditType(String value) {
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