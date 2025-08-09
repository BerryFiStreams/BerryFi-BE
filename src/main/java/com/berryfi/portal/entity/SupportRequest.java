package com.berryfi.portal.entity;

import com.berryfi.portal.enums.SupportPriority;
import com.berryfi.portal.enums.SupportStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a support request.
 */
@Entity
@Table(name = "support_requests",
       indexes = {
           @Index(name = "idx_support_organization", columnList = "organizationId"),
           @Index(name = "idx_support_status", columnList = "status"),
           @Index(name = "idx_support_priority", columnList = "priority"),
           @Index(name = "idx_support_created", columnList = "createdAt")
       })
public class SupportRequest {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(name = "organization_id", nullable = false)
    private String organizationId;
    
    @Column(name = "workspace_id")
    private String workspaceId;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportPriority priority;
    
    @Column(nullable = false)
    private String category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportStatus status;
    
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "assigned_to")
    private String assignedTo;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public SupportRequest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = SupportStatus.OPEN;
        this.priority = SupportPriority.MEDIUM;
    }

    public SupportRequest(String id, String organizationId, String subject, 
                         String message, String contactEmail, String createdBy) {
        this();
        this.id = id;
        this.organizationId = organizationId;
        this.subject = subject;
        this.message = message;
        this.contactEmail = contactEmail;
        this.createdBy = createdBy;
        this.category = "billing";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SupportPriority getPriority() {
        return priority;
    }

    public void setPriority(SupportPriority priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public SupportStatus getStatus() {
        return status;
    }

    public void setStatus(SupportStatus status) {
        this.status = status;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
