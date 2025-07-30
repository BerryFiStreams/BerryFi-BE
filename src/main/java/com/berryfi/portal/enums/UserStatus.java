package com.berryfi.portal.enums;

/**
 * Enum representing user account status.
 */
public enum UserStatus {
    ACTIVE("active", "Active"),
    INVITED("invited", "Invited"),
    DISABLED("disabled", "Disabled");

    private final String value;
    private final String displayName;

    UserStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserStatus fromValue(String value) {
        for (UserStatus status : UserStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid user status value: " + value);
    }
}
