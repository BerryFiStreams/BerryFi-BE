package com.berryfi.portal.enums;

/**
 * Enumeration for transaction types.
 */
public enum TransactionType {
    CREDIT_ADDED("credit_added"),
    RECHARGE("recharge"),
    USAGE("usage"),
    ALLOCATION("allocation"),
    REFUND("refund");

    private final String value;

    TransactionType(String value) {
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
