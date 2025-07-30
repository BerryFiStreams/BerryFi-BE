package com.berryfi.portal.enums;

/**
 * Enum representing workspace status values.
 */
public enum WorkspaceStatus {
    ACTIVE("active", "Active"),
    SUSPENDED("suspended", "Suspended"),
    DISABLED("disabled", "Disabled");

    private final String value;
    private final String displayName;

    WorkspaceStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static WorkspaceStatus fromValue(String value) {
        for (WorkspaceStatus status : WorkspaceStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown workspace status: " + value);
    }
}
