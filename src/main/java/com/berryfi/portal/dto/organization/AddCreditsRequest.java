package com.berryfi.portal.dto.organization;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adding credits to an organization.
 */
public class AddCreditsRequest {
    
    @NotNull(message = "Credits amount is required")
    @DecimalMin(value = "0.01", message = "Credits must be at least 0.01")
    private Double credits;
    
    private boolean purchased = true;

    // Constructors
    public AddCreditsRequest() {}

    public AddCreditsRequest(Double credits, boolean purchased) {
        this.credits = credits;
        this.purchased = purchased;
    }

    // Getters and Setters
    public Double getCredits() {
        return credits;
    }

    public void setCredits(Double credits) {
        this.credits = credits;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }
}