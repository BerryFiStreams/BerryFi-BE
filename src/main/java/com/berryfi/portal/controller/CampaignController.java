package com.berryfi.portal.controller;

import com.berryfi.portal.dto.team.CampaignResponse;
import com.berryfi.portal.dto.team.CreateCampaignRequest;
import com.berryfi.portal.dto.team.UpdateCampaignRequest;
import com.berryfi.portal.service.CampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for campaign management operations.
 */
@RestController
@RequestMapping("/api/team/campaigns")
@CrossOrigin(origins = "*")
public class CampaignController {
    
    @Autowired
    private CampaignService campaignService;
    
    /**
     * Create a new campaign.
     */
    @PostMapping
    public ResponseEntity<CampaignResponse> createCampaign(
            @Valid @RequestBody CreateCampaignRequest request,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            CampaignResponse response = campaignService.createCampaign(request, userId, organizationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get campaign by ID.
     */
    @GetMapping("/{campaignId}")
    public ResponseEntity<CampaignResponse> getCampaign(
            @PathVariable String campaignId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            CampaignResponse response = campaignService.getCampaign(campaignId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get campaigns by organization with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<CampaignResponse>> getCampaigns(
            @RequestHeader("X-Organization-ID") String organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CampaignResponse> campaigns = campaignService.getCampaigns(organizationId, pageable);
        return ResponseEntity.ok(campaigns);
    }
    
    /**
     * Search campaigns.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CampaignResponse>> searchCampaigns(
            @RequestHeader("X-Organization-ID") String organizationId,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CampaignResponse> campaigns = campaignService.searchCampaigns(organizationId, q, pageable);
        return ResponseEntity.ok(campaigns);
    }
    
    /**
     * Get campaigns by project.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<CampaignResponse>> getCampaignsByProject(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CampaignResponse> campaigns = campaignService.getCampaignsByProject(projectId, pageable);
        return ResponseEntity.ok(campaigns);
    }
    
    /**
     * Get active campaigns.
     */
    @GetMapping("/active")
    public ResponseEntity<List<CampaignResponse>> getActiveCampaigns(
            @RequestHeader("X-Organization-ID") String organizationId) {
        List<CampaignResponse> campaigns = campaignService.getActiveCampaigns(organizationId);
        return ResponseEntity.ok(campaigns);
    }
    
    /**
     * Update campaign.
     */
    @PutMapping("/{campaignId}")
    public ResponseEntity<CampaignResponse> updateCampaign(
            @PathVariable String campaignId,
            @Valid @RequestBody UpdateCampaignRequest request,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            CampaignResponse response = campaignService.updateCampaign(campaignId, request, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete campaign.
     */
    @DeleteMapping("/{campaignId}")
    public ResponseEntity<Void> deleteCampaign(
            @PathVariable String campaignId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            campaignService.deleteCampaign(campaignId, organizationId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Record a visit to the campaign (for analytics).
     */
    @PostMapping("/{campaignId}/visit")
    public ResponseEntity<Void> recordVisit(@PathVariable String campaignId) {
        try {
            campaignService.recordVisit(campaignId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get campaign analytics.
     */
    @GetMapping("/analytics")
    public ResponseEntity<CampaignService.CampaignAnalytics> getCampaignAnalytics(
            @RequestHeader("X-Organization-ID") String organizationId) {
        CampaignService.CampaignAnalytics analytics = campaignService.getCampaignAnalytics(organizationId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Pause campaign.
     * POST /api/team/campaigns/{campaignId}/pause
     */
    @PostMapping("/{campaignId}/pause")
    public ResponseEntity<CampaignResponse> pauseCampaign(
            @PathVariable String campaignId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            CampaignResponse response = campaignService.pauseCampaign(campaignId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Resume campaign.
     * POST /api/team/campaigns/{campaignId}/resume
     */
    @PostMapping("/{campaignId}/resume")
    public ResponseEntity<CampaignResponse> resumeCampaign(
            @PathVariable String campaignId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            CampaignResponse response = campaignService.resumeCampaign(campaignId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Copy campaign link.
     * POST /api/team/campaigns/{campaignId}/copy-link
     */
    @PostMapping("/{campaignId}/copy-link")
    public ResponseEntity<CampaignLinkResponse> copyCampaignLink(
            @PathVariable String campaignId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            String url = campaignService.getCampaignUrl(campaignId, organizationId);
            return ResponseEntity.ok(new CampaignLinkResponse(url));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Response DTO for campaign link copy operation.
     */
    public static class CampaignLinkResponse {
        private boolean success = true;
        private CampaignLinkData data;

        public CampaignLinkResponse(String url) {
            this.data = new CampaignLinkData(url);
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public CampaignLinkData getData() { return data; }
        public void setData(CampaignLinkData data) { this.data = data; }

        public static class CampaignLinkData {
            private String url;

            public CampaignLinkData(String url) {
                this.url = url;
            }

            public String getUrl() { return url; }
            public void setUrl(String url) { this.url = url; }
        }
    }
}
