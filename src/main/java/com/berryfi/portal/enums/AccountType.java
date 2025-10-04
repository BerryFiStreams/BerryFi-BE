package com.berryfi.portal.enums;

/**
 * Enum representing account types in the system.
 */
public enum AccountType {
    ORGANIZATION("organization", "Organization");

    private final String value;
    private final String displayName;

    AccountType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AccountType fromValue(String value) {
        for (AccountType type : AccountType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid account type value: " + value);
    }
}
