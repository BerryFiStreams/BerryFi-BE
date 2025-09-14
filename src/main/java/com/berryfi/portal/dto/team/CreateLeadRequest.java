package com.berryfi.portal.dto.team;

import com.berryfi.portal.enums.LeadStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a new lead.
 */
public class CreateLeadRequest {
    
    @NotBlank(message = "Campaign ID is required")
    private String campaignId;
    
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;
    
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;
    
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;
    
    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    private String company;
    
    @Size(max = 50, message = "Job title cannot exceed 50 characters")
    private String jobTitle;
    
    private LeadStatus status = LeadStatus.NEW;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    // Additional fields
    private String source;
    private String medium;
    private String campaign;
    private String term;
    private String content;
    
    // Visit tracking fields
    private Integer visitCount = 0;
    private Long totalSessionTime = 0L;
    private LocalDateTime lastVisitDate;
    
    // Default constructor
    public CreateLeadRequest() {}
    
    // Getters and Setters
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    
    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }
    
    public String getCampaign() { return campaign; }
    public void setCampaign(String campaign) { this.campaign = campaign; }
    
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    // Visit tracking getters and setters
    public Integer getVisitCount() { return visitCount; }
    public void setVisitCount(Integer visitCount) { this.visitCount = visitCount; }
    
    public Long getTotalSessionTime() { return totalSessionTime; }
    public void setTotalSessionTime(Long totalSessionTime) { this.totalSessionTime = totalSessionTime; }
    
    public LocalDateTime getLastVisitDate() { return lastVisitDate; }
    public void setLastVisitDate(LocalDateTime lastVisitDate) { this.lastVisitDate = lastVisitDate; }
}
