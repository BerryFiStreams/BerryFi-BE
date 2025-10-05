package com.berryfi.portal.dto.project;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for allocating credits to a project.
 */
public class AllocateCreditsRequest {

    @NotNull(message = "Credits amount is required")
    @Positive(message = "Credits must be positive")
    private Double credits;

    public AllocateCreditsRequest() {}

    public AllocateCreditsRequest(Double credits) {
        this.credits = credits;
    }

    public Double getCredits() {
        return credits;
    }

    public void setCredits(Double credits) {
        this.credits = credits;
    }
}