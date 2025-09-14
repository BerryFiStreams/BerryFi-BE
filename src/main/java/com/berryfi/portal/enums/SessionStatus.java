package com.berryfi.portal.enums;

/**
 * Enumeration for VM session status.
 */
public enum SessionStatus {
    REQUESTED("requested"),          // Session requested but not started
    STARTING("starting"),            // Session is being initialized
    ACTIVE("active"),               // Session is active and VM is running
    IDLE("idle"),                   // Session is active but no recent heartbeat
    TERMINATING("terminating"),     // Session is being terminated
    COMPLETED("completed"),         // Session completed successfully
    TERMINATED("terminated"),       // Session terminated (timeout/manual)
    FAILED("failed");              // Session failed to start or had an error

    private final String value;

    SessionStatus(String value) {
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
