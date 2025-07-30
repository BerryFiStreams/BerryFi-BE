package com.berryfi.portal.service;

import com.berryfi.portal.dto.team.CreateLeadRequest;
import com.berryfi.portal.dto.team.LeadResponse;
import com.berryfi.portal.dto.team.UpdateLeadRequest;
import com.berryfi.portal.entity.Campaign;
import com.berryfi.portal.entity.Lead;
import com.berryfi.portal.enums.LeadStatus;
import com.berryfi.portal.repository.CampaignRepository;
import com.berryfi.portal.repository.LeadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing leads.
 */
@Service
public class LeadService {
    
    @Autowired
    private LeadRepository leadRepository;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private CampaignService campaignService;
    
    /**
     * Create a new lead.
     */
    public LeadResponse createLead(CreateLeadRequest request, String userId, String organizationId) {
        // Validate campaign exists
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        if (!campaign.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Campaign not found in organization");
        }
        
        // Check for duplicate email
        List<Lead> existingLeads = leadRepository.findByOrganizationIdAndEmail(organizationId, request.getEmail());
        if (!existingLeads.isEmpty()) {
            throw new RuntimeException("Lead with this email already exists");
        }
        
        Lead lead = new Lead();
        lead.setId(UUID.randomUUID().toString());
        lead.setFirstName(request.getFirstName());
        lead.setLastName(request.getLastName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setCompany(request.getCompany());
        lead.setJobTitle(request.getJobTitle());
        lead.setCampaignId(request.getCampaignId());
        lead.setCampaignName(campaign.getName());
        lead.setProjectId(campaign.getProjectId());
        lead.setProjectName(campaign.getProjectName());
        lead.setOrganizationId(organizationId);
        lead.setWorkspaceId(campaign.getWorkspaceId());
        lead.setStatus(request.getStatus() != null ? request.getStatus() : LeadStatus.NEW);
        lead.setCreatedBy(userId);
        
        // Set tracking information
        lead.setSource(request.getSource());
        lead.setMedium(request.getMedium());
        lead.setCampaign(request.getCampaign());
        
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            lead.addNote(request.getNotes());
        }
        
        Lead savedLead = leadRepository.save(lead);
        
        // Update campaign lead count
        campaignService.recordLead(request.getCampaignId());
        
        return mapToResponse(savedLead);
    }
    
    /**
     * Update an existing lead.
     */
    public LeadResponse updateLead(String leadId, UpdateLeadRequest request, String organizationId) {
        Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        if (!lead.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Lead not found in organization");
        }
        
        // Update fields if provided
        if (request.getFirstName() != null) {
            lead.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            lead.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            // Check for duplicate email
            List<Lead> existingLeads = leadRepository.findByOrganizationIdAndEmail(organizationId, request.getEmail());
            if (!existingLeads.isEmpty() && !existingLeads.get(0).getId().equals(leadId)) {
                throw new RuntimeException("Lead with this email already exists");
            }
            lead.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            lead.setPhone(request.getPhone());
        }
        if (request.getCompany() != null) {
            lead.setCompany(request.getCompany());
        }
        if (request.getJobTitle() != null) {
            lead.setJobTitle(request.getJobTitle());
        }
        if (request.getStatus() != null) {
            lead.setStatus(request.getStatus());
            if (request.getStatus() == LeadStatus.CONVERTED && !lead.getIsConverted()) {
                lead.markAsConverted();
                campaignService.recordConversion(lead.getCampaignId());
            }
        }
        if (request.getNotes() != null) {
            lead.addNote(request.getNotes());
        }
        if (request.getScore() != null) {
            lead.updateLeadScore(request.getScore());
        }
        if (request.getAssignedTo() != null) {
            lead.setAssignedTo(request.getAssignedTo());
        }
        if (request.getLastContactedAt() != null) {
            lead.setLastContactedAt(request.getLastContactedAt());
        }
        
        Lead updatedLead = leadRepository.save(lead);
        return mapToResponse(updatedLead);
    }
    
    /**
     * Get lead by ID.
     */
    public LeadResponse getLead(String leadId, String organizationId) {
        Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        if (!lead.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Lead not found in organization");
        }
        
        return mapToResponse(lead);
    }
    
    /**
     * Get leads by organization with pagination.
     */
    public Page<LeadResponse> getLeads(String organizationId, Pageable pageable) {
        Page<Lead> leads = leadRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, pageable);
        return leads.map(this::mapToResponse);
    }
    
    /**
     * Search leads.
     */
    public Page<LeadResponse> searchLeads(String organizationId, String searchTerm, Pageable pageable) {
        Page<Lead> leads = leadRepository.searchLeads(organizationId, searchTerm, pageable);
        return leads.map(this::mapToResponse);
    }
    
    /**
     * Get leads by campaign.
     */
    public Page<LeadResponse> getLeadsByCampaign(String campaignId, Pageable pageable) {
        Page<Lead> leads = leadRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId, pageable);
        return leads.map(this::mapToResponse);
    }
    
    /**
     * Get leads by status.
     */
    public Page<LeadResponse> getLeadsByStatus(String organizationId, LeadStatus status, Pageable pageable) {
        Page<Lead> leads = leadRepository.findByOrganizationIdAndStatusOrderByCreatedAtDesc(organizationId, status, pageable);
        return leads.map(this::mapToResponse);
    }
    
    /**
     * Get leads assigned to user.
     */
    public Page<LeadResponse> getLeadsByAssignee(String assignedTo, Pageable pageable) {
        Page<Lead> leads = leadRepository.findByAssignedToOrderByCreatedAtDesc(assignedTo, pageable);
        return leads.map(this::mapToResponse);
    }
    
    /**
     * Get unassigned leads.
     */
    public Page<LeadResponse> getUnassignedLeads(String organizationId, Pageable pageable) {
        Page<Lead> leads = leadRepository.findByOrganizationIdAndAssignedToIsNullOrderByCreatedAtDesc(organizationId, pageable);
        return leads.map(this::mapToResponse);
    }
    
    /**
     * Delete lead.
     */
    public void deleteLead(String leadId, String organizationId) {
        Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        if (!lead.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Lead not found in organization");
        }
        
        leadRepository.delete(lead);
    }
    
    /**
     * Assign lead to user.
     */
    public LeadResponse assignLead(String leadId, String assignedTo, String organizationId) {
        Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        if (!lead.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Lead not found in organization");
        }
        
        lead.setAssignedTo(assignedTo);
        Lead updatedLead = leadRepository.save(lead);
        return mapToResponse(updatedLead);
    }
    
    /**
     * Get lead analytics.
     */
    public LeadAnalytics getLeadAnalytics(String organizationId) {
        LeadAnalytics analytics = new LeadAnalytics();
        analytics.setTotalLeads(leadRepository.getTotalLeadsCount(organizationId));
        analytics.setConvertedLeads(leadRepository.getConvertedLeadsCount(organizationId));
        analytics.setNewLeads(leadRepository.countByOrganizationIdAndStatus(organizationId, LeadStatus.NEW));
        analytics.setQualifiedLeads(leadRepository.countByOrganizationIdAndStatus(organizationId, LeadStatus.QUALIFIED));
        analytics.setContactedLeads(leadRepository.countByOrganizationIdAndStatus(organizationId, LeadStatus.CONTACTED));
        analytics.setAverageLeadScore(leadRepository.getAverageLeadScore(organizationId));
        
        // Calculate conversion rate
        if (analytics.getTotalLeads() > 0) {
            analytics.setConversionRate((double) analytics.getConvertedLeads() / analytics.getTotalLeads() * 100);
        } else {
            analytics.setConversionRate(0.0);
        }
        
        return analytics;
    }
    
    /**
     * Get leads needing follow-up.
     */
    public List<LeadResponse> getLeadsNeedingFollowUp(String organizationId, int daysSinceContact) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysSinceContact);
        List<LeadStatus> statuses = List.of(LeadStatus.NEW, LeadStatus.QUALIFIED, LeadStatus.CONTACTED);
        List<Lead> leads = leadRepository.findLeadsNeedingFollowUp(organizationId, statuses, cutoffDate);
        return leads.stream().map(this::mapToResponse).toList();
    }
    
    /**
     * Map Lead entity to LeadResponse DTO.
     */
    private LeadResponse mapToResponse(Lead lead) {
        LeadResponse response = new LeadResponse();
        response.setId(lead.getId());
        response.setCampaignId(lead.getCampaignId());
        response.setCampaignName(lead.getCampaignName());
        response.setFirstName(lead.getFirstName());
        response.setLastName(lead.getLastName());
        response.setEmail(lead.getEmail());
        response.setPhone(lead.getPhone());
        response.setCompany(lead.getCompany());
        response.setJobTitle(lead.getJobTitle());
        response.setStatus(lead.getStatus());
        response.setNotes(lead.getNotes() != null ? String.join("; ", lead.getNotes()) : null);
        response.setSource(lead.getSource());
        response.setMedium(lead.getMedium());
        response.setCampaign(lead.getCampaign());
        response.setIpAddress(lead.getIpAddress());
        response.setUserAgent(lead.getCustomFields()); // Using customFields for userAgent temporarily
        response.setReferrer(lead.getReferrer());
        response.setScore(lead.getLeadScore());
        response.setOrganizationId(lead.getOrganizationId());
        response.setWorkspaceId(lead.getWorkspaceId());
        response.setAssignedTo(lead.getAssignedTo());
        response.setCreatedBy(lead.getCreatedBy());
        response.setCreatedAt(lead.getCreatedAt());
        response.setUpdatedAt(lead.getUpdatedAt());
        response.setLastContactedAt(lead.getLastContactedAt());
        return response;
    }
    
    /**
     * Inner class for lead analytics data.
     */
    public static class LeadAnalytics {
        private Long totalLeads;
        private Long convertedLeads;
        private Long newLeads;
        private Long qualifiedLeads;
        private Long contactedLeads;
        private Double averageLeadScore;
        private Double conversionRate;
        
        // Getters and setters
        public Long getTotalLeads() { return totalLeads; }
        public void setTotalLeads(Long totalLeads) { this.totalLeads = totalLeads; }
        
        public Long getConvertedLeads() { return convertedLeads; }
        public void setConvertedLeads(Long convertedLeads) { this.convertedLeads = convertedLeads; }
        
        public Long getNewLeads() { return newLeads; }
        public void setNewLeads(Long newLeads) { this.newLeads = newLeads; }
        
        public Long getQualifiedLeads() { return qualifiedLeads; }
        public void setQualifiedLeads(Long qualifiedLeads) { this.qualifiedLeads = qualifiedLeads; }
        
        public Long getContactedLeads() { return contactedLeads; }
        public void setContactedLeads(Long contactedLeads) { this.contactedLeads = contactedLeads; }
        
        public Double getAverageLeadScore() { return averageLeadScore; }
        public void setAverageLeadScore(Double averageLeadScore) { this.averageLeadScore = averageLeadScore; }
        
        public Double getConversionRate() { return conversionRate; }
        public void setConversionRate(Double conversionRate) { this.conversionRate = conversionRate; }
    }
}
