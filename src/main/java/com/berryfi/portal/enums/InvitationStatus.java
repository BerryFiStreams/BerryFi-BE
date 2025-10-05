package com.berryfi.portal.enums;

/**
 * Enum representing the status of a project invitation.
 */
public enum InvitationStatus {
    PENDING("pending", "Invitation sent, awaiting response"),
    ACCEPTED("accepted", "Invitation accepted, user registered"),
    DECLINED("declined", "Invitation declined by recipient"),
    EXPIRED("expired", "Invitation expired without response"),
    CANCELLED("cancelled", "Invitation cancelled by sender");

    private final String value;
    private final String description;

    InvitationStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static InvitationStatus fromValue(String value) {
        for (InvitationStatus status : InvitationStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid invitation status value: " + value);
    }

    public boolean isActive() {
        return this == PENDING;
    }

    public boolean isCompleted() {
        return this == ACCEPTED || this == DECLINED || this == EXPIRED || this == CANCELLED;
    }
}