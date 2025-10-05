package com.berryfi.portal.enums;

/**
 * VM types supported by the system.
 * Billing rates differ for each VM type.
 */
public enum VmType {
    T4("T4", "Tesla T4 GPU VM"),
    A10("A10", "Tesla A10 GPU VM");

    private final String value;
    private final String displayName;

    VmType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static VmType fromValue(String value) {
        for (VmType vmType : VmType.values()) {
            if (vmType.value.equals(value)) {
                return vmType;
            }
        }
        throw new IllegalArgumentException("Invalid VM type value: " + value);
    }

    @Override
    public String toString() {
        return this.value;
    }
}