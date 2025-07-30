package com.berryfi.portal.enums;

/**
 * Enum representing project status.
 */
public enum ProjectStatus {
    RUNNING("running", "Running"),
    STOPPED("stopped", "Stopped"),
    DEPLOYING("deploying", "Deploying"),
    ERROR("error", "Error");

    private final String value;
    private final String displayName;

    ProjectStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ProjectStatus fromValue(String value) {
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid project status value: " + value);
    }
}
