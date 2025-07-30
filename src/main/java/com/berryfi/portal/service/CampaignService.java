package com.berryfi.portal.service;

import com.berryfi.portal.dto.team.CampaignResponse;
import com.berryfi.portal.dto.team.CreateCampaignRequest;
import com.berryfi.portal.dto.team.UpdateCampaignRequest;
import com.berryfi.portal.entity.Campaign;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.enums.AccessType;
import com.berryfi.portal.enums.CampaignStatus;
import com.berryfi.portal.repository.CampaignRepository;
import com.berryfi.portal.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service class for managing campaigns.
 */
@Service
public class CampaignService {
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    /**
     * Create a new campaign.
     */
    public CampaignResponse createCampaign(CreateCampaignRequest request, String userId, String organizationId) {
        // Validate project exists
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // Check if campaign name already exists
        if (campaignRepository.existsByOrganizationIdAndName(organizationId, request.getName())) {
            throw new RuntimeException("Campaign with this name already exists");
        }
        
        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID().toString());
        campaign.setName(request.getName());
        campaign.setCustomName(request.getCustomName());
        campaign.setProjectId(request.getProjectId());
        campaign.setProjectName(project.getName());
        campaign.setAccessType(request.getAccessType() != null ? request.getAccessType() : AccessType.DIRECT);
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setDescription(request.getDescription());
        campaign.setOrganizationId(organizationId);
        campaign.setWorkspaceId(project.getWorkspaceId());
        campaign.setCreatedBy(userId);
        
        // Set lead capture settings
        campaign.setRequireFirstName(request.getRequireFirstName() != null ? request.getRequireFirstName() : false);
        campaign.setRequireLastName(request.getRequireLastName() != null ? request.getRequireLastName() : false);
        campaign.setRequireEmail(request.getRequireEmail() != null ? request.getRequireEmail() : true);
        campaign.setRequirePhone(request.getRequirePhone() != null ? request.getRequirePhone() : false);
        campaign.setEnableOTP(request.getEnableOTP() != null ? request.getEnableOTP() : false);
        
        // Generate campaign URL
        campaign.generateUrl("https://berryfi.com"); // TODO: Make this configurable
        
        Campaign savedCampaign = campaignRepository.save(campaign);
        return mapToResponse(savedCampaign);
    }
    
    /**
     * Update an existing campaign.
     */
    public CampaignResponse updateCampaign(String campaignId, UpdateCampaignRequest request, String organizationId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        if (!campaign.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Campaign not found in organization");
        }
        
        // Update fields if provided
        if (request.getName() != null) {
            // Check name uniqueness
            if (!campaign.getName().equals(request.getName()) && 
                campaignRepository.existsByOrganizationIdAndName(organizationId, request.getName())) {
                throw new RuntimeException("Campaign with this name already exists");
            }
            campaign.setName(request.getName());
        }
        
        if (request.getCustomName() != null) {
            campaign.setCustomName(request.getCustomName());
        }
        
        if (request.getAccessType() != null) {
            campaign.setAccessType(request.getAccessType());
        }
        
        if (request.getStatus() != null) {
            campaign.setStatus(request.getStatus());
        }
        
        if (request.getDescription() != null) {
            campaign.setDescription(request.getDescription());
        }
        
        // Update lead capture settings
        if (request.getRequireFirstName() != null) {
            campaign.setRequireFirstName(request.getRequireFirstName());
        }
        if (request.getRequireLastName() != null) {
            campaign.setRequireLastName(request.getRequireLastName());
        }
        if (request.getRequireEmail() != null) {
            campaign.setRequireEmail(request.getRequireEmail());
        }
        if (request.getRequirePhone() != null) {
            campaign.setRequirePhone(request.getRequirePhone());
        }
        if (request.getEnableOTP() != null) {
            campaign.setEnableOTP(request.getEnableOTP());
        }
        
        Campaign updatedCampaign = campaignRepository.save(campaign);
        return mapToResponse(updatedCampaign);
    }
    
    /**
     * Get campaign by ID.
     */
    public CampaignResponse getCampaign(String campaignId, String organizationId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        if (!campaign.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Campaign not found in organization");
        }
        
        return mapToResponse(campaign);
    }
    
    /**
     * Get campaigns by organization with pagination.
     */
    public Page<CampaignResponse> getCampaigns(String organizationId, Pageable pageable) {
        Page<Campaign> campaigns = campaignRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, pageable);
        return campaigns.map(this::mapToResponse);
    }
    
    /**
     * Search campaigns.
     */
    public Page<CampaignResponse> searchCampaigns(String organizationId, String searchTerm, Pageable pageable) {
        Page<Campaign> campaigns = campaignRepository.searchByName(organizationId, searchTerm, pageable);
        return campaigns.map(this::mapToResponse);
    }
    
    /**
     * Get campaigns by project.
     */
    public Page<CampaignResponse> getCampaignsByProject(String projectId, Pageable pageable) {
        Page<Campaign> campaigns = campaignRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable);
        return campaigns.map(this::mapToResponse);
    }
    
    /**
     * Get active campaigns.
     */
    public List<CampaignResponse> getActiveCampaigns(String organizationId) {
        List<Campaign> campaigns = campaignRepository.findActiveCampaigns(organizationId);
        return campaigns.stream().map(this::mapToResponse).toList();
    }
    
    /**
     * Delete campaign.
     */
    public void deleteCampaign(String campaignId, String organizationId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        if (!campaign.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Campaign not found in organization");
        }
        
        campaignRepository.delete(campaign);
    }
    
    /**
     * Record a visit to the campaign.
     */
    public void recordVisit(String campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        campaign.incrementVisits();
        campaignRepository.save(campaign);
    }
    
    /**
     * Record a lead generation for the campaign.
     */
    public void recordLead(String campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        campaign.incrementLeads();
        campaignRepository.save(campaign);
    }
    
    /**
     * Record a conversion for the campaign.
     */
    public void recordConversion(String campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        campaign.incrementConversions();
        campaignRepository.save(campaign);
    }
    
    /**
     * Get campaign analytics.
     */
    public CampaignAnalytics getCampaignAnalytics(String organizationId) {
        CampaignAnalytics analytics = new CampaignAnalytics();
        
        // Count campaigns by status - using ACTIVE status count as proxy for total
        analytics.setActiveCampaigns(campaignRepository.countByOrganizationIdAndStatus(organizationId, CampaignStatus.ACTIVE));
        Long pausedCampaigns = campaignRepository.countByOrganizationIdAndStatus(organizationId, CampaignStatus.PAUSED);
        Long inactiveCampaigns = campaignRepository.countByOrganizationIdAndStatus(organizationId, CampaignStatus.INACTIVE);
        analytics.setTotalCampaigns(analytics.getActiveCampaigns() + pausedCampaigns + inactiveCampaigns);
        
        analytics.setTotalVisits(campaignRepository.getTotalVisits(organizationId));
        analytics.setTotalLeads(campaignRepository.getTotalLeads(organizationId));
        analytics.setTotalConversions(campaignRepository.getTotalConversions(organizationId));
        analytics.setAverageConversionRate(campaignRepository.getAverageConversionRate(organizationId));
        return analytics;
    }
    
    /**
     * Map Campaign entity to CampaignResponse DTO.
     */
    private CampaignResponse mapToResponse(Campaign campaign) {
        CampaignResponse response = new CampaignResponse();
        response.setId(campaign.getId());
        response.setName(campaign.getName());
        response.setCustomName(campaign.getCustomName());
        response.setProjectId(campaign.getProjectId());
        response.setProjectName(campaign.getProjectName());
        response.setAccessType(campaign.getAccessType());
        response.setStatus(campaign.getStatus());
        response.setDescription(campaign.getDescription());
        response.setUrl(campaign.getUrl());
        response.setRequireFirstName(campaign.getRequireFirstName());
        response.setRequireLastName(campaign.getRequireLastName());
        response.setRequireEmail(campaign.getRequireEmail());
        response.setRequirePhone(campaign.getRequirePhone());
        response.setEnableOTP(campaign.getEnableOTP());
        response.setVisits(campaign.getVisits());
        response.setLeads(campaign.getLeads());
        response.setConversions(campaign.getConversions());
        response.setConversionRate(campaign.getConversionRate());
        response.setOrganizationId(campaign.getOrganizationId());
        response.setWorkspaceId(campaign.getWorkspaceId());
        response.setCreatedBy(campaign.getCreatedBy());
        response.setCreatedAt(campaign.getCreatedAt());
        response.setUpdatedAt(campaign.getUpdatedAt());
        return response;
    }
    
    /**
     * Inner class for campaign analytics data.
     */
    public static class CampaignAnalytics {
        private Long totalCampaigns;
        private Long activeCampaigns;
        private Long totalVisits;
        private Long totalLeads;
        private Long totalConversions;
        private Double averageConversionRate;
        
        // Getters and setters
        public Long getTotalCampaigns() { return totalCampaigns; }
        public void setTotalCampaigns(Long totalCampaigns) { this.totalCampaigns = totalCampaigns; }
        
        public Long getActiveCampaigns() { return activeCampaigns; }
        public void setActiveCampaigns(Long activeCampaigns) { this.activeCampaigns = activeCampaigns; }
        
        public Long getTotalVisits() { return totalVisits; }
        public void setTotalVisits(Long totalVisits) { this.totalVisits = totalVisits; }
        
        public Long getTotalLeads() { return totalLeads; }
        public void setTotalLeads(Long totalLeads) { this.totalLeads = totalLeads; }
        
        public Long getTotalConversions() { return totalConversions; }
        public void setTotalConversions(Long totalConversions) { this.totalConversions = totalConversions; }
        
        public Double getAverageConversionRate() { return averageConversionRate; }
        public void setAverageConversionRate(Double averageConversionRate) { this.averageConversionRate = averageConversionRate; }
    }
}
