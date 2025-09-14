package com.berryfi.portal.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for number formatting operations.
 */
public class NumberFormatUtil {

    /**
     * Format a Double value to 2 decimal places for display.
     * Returns null if input is null.
     * 
     * @param value The Double value to format
     * @return Formatted Double with 2 decimal places, or null if input is null
     */
    public static Double formatCredits(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Format a currency/monetary value to 2 decimal places for display.
     * Returns null if input is null.
     * 
     * @param value The Double value to format
     * @return Formatted Double with 2 decimal places, or null if input is null
     */
    public static Double formatCurrency(Double value) {
        return formatCredits(value); // Same logic for currency
    }

    /**
     * Format a percentage value to 2 decimal places for display.
     * Returns null if input is null.
     * 
     * @param value The Double value to format
     * @return Formatted Double with 2 decimal places, or null if input is null
     */
    public static Double formatPercentage(Double value) {
        return formatCredits(value); // Same logic for percentage
    }

    /**
     * Format a Double value to specified decimal places.
     * Returns null if input is null.
     * 
     * @param value The Double value to format
     * @param decimalPlaces Number of decimal places
     * @return Formatted Double with specified decimal places, or null if input is null
     */
    public static Double format(Double value, int decimalPlaces) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value)
                .setScale(decimalPlaces, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
