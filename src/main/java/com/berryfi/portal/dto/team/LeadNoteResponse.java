package com.berryfi.portal.dto.team;

import java.time.LocalDateTime;

/**
 * Response DTO for lead note data.
 */
public class LeadNoteResponse {
    
    private String id;
    private String note;
    private LocalDateTime createdAt;
    private String addedBy;
    
    // Default constructor
    public LeadNoteResponse() {}
    
    // Constructor
    public LeadNoteResponse(String id, String note, LocalDateTime createdAt, String addedBy) {
        this.id = id;
        this.note = note;
        this.createdAt = createdAt;
        this.addedBy = addedBy;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getAddedBy() { return addedBy; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }
}
