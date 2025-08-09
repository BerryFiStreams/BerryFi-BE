package com.berryfi.portal.enums;

/**
 * Enumeration for invoice status.
 */
public enum InvoiceStatus {
    PAID("paid"),
    PENDING("pending"),
    OVERDUE("overdue");

    private final String value;

    InvoiceStatus(String value) {
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
