package com.berryfi.portal.enums;

/**
 * Enumeration for VM instance status.
 */
public enum VmStatus {
    AVAILABLE("available"),           // VM is ready to be assigned
    ASSIGNED("assigned"),            // VM is assigned to a session but not started
    STARTING("starting"),            // VM is being started
    RUNNING("running"),              // VM is running and active
    STOPPING("stopping"),            // VM is being stopped
    STOPPED("stopped"),              // VM is stopped
    DEALLOCATED("deallocated"),      // VM is deallocated (Azure specific)
    MAINTENANCE("maintenance"),       // VM is under maintenance
    ERROR("error");                  // VM has an error state

    private final String value;

    VmStatus(String value) {
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
