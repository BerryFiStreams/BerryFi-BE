package com.berryfi.portal.entity;

import com.berryfi.portal.enums.LeadStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a lead captured through campaigns.
 */
@Entity
@Table(name = "leads", indexes = {
    @Index(name = "idx_lead_campaign", columnList = "campaignId"),
    @Index(name = "idx_lead_workspace", columnList = "workspaceId"),
    @Index(name = "idx_lead_organization", columnList = "organizationId"),
    @Index(name = "idx_lead_status", columnList = "status"),
    @Index(name = "idx_lead_email", columnList = "email"),
    @Index(name = "idx_lead_created_at", columnList = "createdAt")
})
public class Lead {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false)
    private String email;
    
    private String phone;
    
    @NotBlank(message = "Campaign ID is required")
    @Column(nullable = false)
    private String campaignId;
    
    @NotBlank(message = "Campaign name is required")
    @Column(nullable = false)
    private String campaignName;
    
    @NotBlank(message = "Project ID is required")
    @Column(nullable = false)
    private String projectId;
    
    @NotBlank(message = "Project name is required")
    @Column(nullable = false)
    private String projectName;
    
    @NotBlank(message = "Organization ID is required")
    @Column(nullable = false)
    private String organizationId;
    
    private String workspaceId;
    
    @NotNull(message = "Lead status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status = LeadStatus.NEW;
    
    // Contact information
    private String company;
    private String jobTitle;
    private String website;
    private String linkedinUrl;
    
    // Lead source tracking
    private String source; // e.g., "direct", "google", "facebook"
    private String medium; // e.g., "organic", "cpc", "email"
    private String campaign; // marketing campaign name
    private String referrer; // referring URL
    
    // Geographic information
    private String country;
    private String state;
    private String city;
    private String ipAddress;
    
    // Engagement tracking
    private Integer sessionDuration = 0; // in seconds
    
    private Integer pageViews = 0;
    
    // Visit tracking fields
    private Integer visitCount = 0;
    
    private Long totalSessionTime = 0L; // cumulative time in seconds across all visits
    
    private LocalDateTime lastVisitDate;
    
    private Boolean isConverted = false;
    
    private LocalDateTime convertedAt;
    
    // Lead scoring
    private Integer leadScore = 0;
    
    // Custom fields for additional data
    @Column(columnDefinition = "TEXT")
    private String customFields; // JSON string for flexible custom data
    
    // Notes and interactions
    @ElementCollection
    @CollectionTable(name = "lead_notes", joinColumns = @JoinColumn(name = "lead_id"))
    @Column(columnDefinition = "TEXT")
    private List<String> notes = new ArrayList<>();
    
    @NotBlank(message = "Created by is required")
    @Column(nullable = false)
    private String createdBy; // user who created this lead
    
    private String assignedTo; // user assigned to follow up
    
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastContactedAt;
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public void addNote(String note) {
        if (this.notes == null) {
            this.notes = new ArrayList<>();
        }
        this.notes.add(note);
    }
    
    public void markAsConverted() {
        this.isConverted = true;
        this.convertedAt = LocalDateTime.now();
        this.status = LeadStatus.CONVERTED;
    }
    
    public void updateLeadScore(int score) {
        this.leadScore = Math.max(0, Math.min(100, score)); // Keep score between 0-100
    }
    
    public boolean isHighPriority() {
        return this.leadScore >= 80;
    }
    
    public boolean isMediumPriority() {
        return this.leadScore >= 50 && this.leadScore < 80;
    }
    
    public boolean isLowPriority() {
        return this.leadScore < 50;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    
    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }
    
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }
    
    public String getCampaign() { return campaign; }
    public void setCampaign(String campaign) { this.campaign = campaign; }
    
    public String getReferrer() { return referrer; }
    public void setReferrer(String referrer) { this.referrer = referrer; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public Integer getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(Integer sessionDuration) { this.sessionDuration = sessionDuration; }
    
    public Integer getPageViews() { return pageViews; }
    public void setPageViews(Integer pageViews) { this.pageViews = pageViews; }
    
    public Boolean getIsConverted() { return isConverted; }
    public void setIsConverted(Boolean isConverted) { this.isConverted = isConverted; }
    
    public LocalDateTime getConvertedAt() { return convertedAt; }
    public void setConvertedAt(LocalDateTime convertedAt) { this.convertedAt = convertedAt; }
    
    public Integer getLeadScore() { return leadScore; }
    public void setLeadScore(Integer leadScore) { this.leadScore = leadScore; }
    
    public String getCustomFields() { return customFields; }
    public void setCustomFields(String customFields) { this.customFields = customFields; }
    
    public List<String> getNotes() { return notes; }
    public void setNotes(List<String> notes) { this.notes = notes; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastContactedAt() { return lastContactedAt; }
    public void setLastContactedAt(LocalDateTime lastContactedAt) { this.lastContactedAt = lastContactedAt; }
    
    // Visit tracking getters and setters
    public Integer getVisitCount() { return visitCount; }
    public void setVisitCount(Integer visitCount) { this.visitCount = visitCount; }
    
    public Long getTotalSessionTime() { return totalSessionTime; }
    public void setTotalSessionTime(Long totalSessionTime) { this.totalSessionTime = totalSessionTime; }
    
    public LocalDateTime getLastVisitDate() { return lastVisitDate; }
    public void setLastVisitDate(LocalDateTime lastVisitDate) { this.lastVisitDate = lastVisitDate; }
}
