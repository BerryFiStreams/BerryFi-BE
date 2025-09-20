package com.berryfi.portal.dto.team;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for adding a note to a lead.
 */
public class AddLeadNoteRequest {
    
    @NotBlank(message = "Note is required")
    private String note;
    
    // Default constructor
    public AddLeadNoteRequest() {}
    
    // Constructor
    public AddLeadNoteRequest(String note) {
        this.note = note;
    }
    
    // Getters and Setters
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
