package com.berryfi.portal.enums;

/**
 * Enum representing budget action values when budget limits are reached.
 */
public enum BudgetAction {
    ALERT("alert", "Alert Only"),
    BLOCK("block", "Block Access"),
    NONE("none", "No Action");

    private final String value;
    private final String displayName;

    BudgetAction(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BudgetAction fromValue(String value) {
        for (BudgetAction action : BudgetAction.values()) {
            if (action.value.equals(value)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown budget action: " + value);
    }
}
