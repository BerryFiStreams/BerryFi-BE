package com.berryfi.portal.controller;

import com.berryfi.portal.dto.team.CreateLeadRequest;
import com.berryfi.portal.dto.team.LeadResponse;
import com.berryfi.portal.dto.team.UpdateLeadRequest;
import com.berryfi.portal.dto.team.AddLeadNoteRequest;
import com.berryfi.portal.dto.team.LeadNoteResponse;
import com.berryfi.portal.enums.LeadStatus;
import com.berryfi.portal.service.LeadService;
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
 * REST controller for lead management operations.
 */
@RestController
@RequestMapping("/api/team/leads")
public class LeadController {
    
    @Autowired
    private LeadService leadService;
    
    /**
     * Create a new lead.
     */
    @PostMapping
    public ResponseEntity<LeadResponse> createLead(
            @Valid @RequestBody CreateLeadRequest request,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            LeadResponse response = leadService.createLead(request, userId, organizationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get lead by ID.
     */
    @GetMapping("/{leadId}")
    public ResponseEntity<LeadResponse> getLead(
            @PathVariable String leadId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            LeadResponse response = leadService.getLead(leadId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get leads by organization with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<LeadResponse>> getLeads(
            @RequestHeader("X-Organization-ID") String organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeadResponse> leads = leadService.getLeads(organizationId, pageable);
        return ResponseEntity.ok(leads);
    }
    
    /**
     * Search leads.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<LeadResponse>> searchLeads(
            @RequestHeader("X-Organization-ID") String organizationId,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeadResponse> leads = leadService.searchLeads(organizationId, q, pageable);
        return ResponseEntity.ok(leads);
    }
    
    /**
     * Get leads by campaign.
     */
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<Page<LeadResponse>> getLeadsByCampaign(
            @PathVariable String campaignId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeadResponse> leads = leadService.getLeadsByCampaign(campaignId, pageable);
        return ResponseEntity.ok(leads);
    }
    
    /**
     * Get leads by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<LeadResponse>> getLeadsByStatus(
            @RequestHeader("X-Organization-ID") String organizationId,
            @PathVariable LeadStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeadResponse> leads = leadService.getLeadsByStatus(organizationId, status, pageable);
        return ResponseEntity.ok(leads);
    }
    
    /**
     * Get leads assigned to user.
     */
    @GetMapping("/assigned/{userId}")
    public ResponseEntity<Page<LeadResponse>> getLeadsByAssignee(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeadResponse> leads = leadService.getLeadsByAssignee(userId, pageable);
        return ResponseEntity.ok(leads);
    }
    
    /**
     * Get unassigned leads.
     */
    @GetMapping("/unassigned")
    public ResponseEntity<Page<LeadResponse>> getUnassignedLeads(
            @RequestHeader("X-Organization-ID") String organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeadResponse> leads = leadService.getUnassignedLeads(organizationId, pageable);
        return ResponseEntity.ok(leads);
    }
    
    /**
     * Update lead.
     */
    @PutMapping("/{leadId}")
    public ResponseEntity<LeadResponse> updateLead(
            @PathVariable String leadId,
            @Valid @RequestBody UpdateLeadRequest request,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            LeadResponse response = leadService.updateLead(leadId, request, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Assign lead to user.
     */
    @PutMapping("/{leadId}/assign")
    public ResponseEntity<LeadResponse> assignLead(
            @PathVariable String leadId,
            @RequestParam String assignedTo,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            LeadResponse response = leadService.assignLead(leadId, assignedTo, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete lead.
     */
    @DeleteMapping("/{leadId}")
    public ResponseEntity<Void> deleteLead(
            @PathVariable String leadId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        try {
            leadService.deleteLead(leadId, organizationId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get leads needing follow-up.
     */
    @GetMapping("/follow-up")
    public ResponseEntity<List<LeadResponse>> getLeadsNeedingFollowUp(
            @RequestHeader("X-Organization-ID") String organizationId,
            @RequestParam(defaultValue = "7") int daysSinceContact) {
        List<LeadResponse> leads = leadService.getLeadsNeedingFollowUp(organizationId, daysSinceContact);
        return ResponseEntity.ok(leads);
    }
    
    /**
     * Add a note to a lead.
     * POST /team/leads/{leadId}/notes
     */
    @PostMapping("/{leadId}/notes")
    public ResponseEntity<LeadNoteResponse> addLeadNote(
            @PathVariable String leadId,
            @Valid @RequestBody AddLeadNoteRequest request,
            @RequestHeader("X-Organization-ID") String organizationId,
            @RequestHeader("X-User-ID") String userId) {
        try {
            leadService.addLeadNote(leadId, request.getNote(), organizationId, userId);
            
            // Create a simple response
            LeadNoteResponse response = new LeadNoteResponse();
            response.setId("note_" + System.currentTimeMillis());
            response.setNote(request.getNote());
            response.setCreatedAt(java.time.LocalDateTime.now());
            response.setAddedBy(userId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get lead analytics.
     */
    @GetMapping("/analytics")
    public ResponseEntity<LeadService.LeadAnalytics> getLeadAnalytics(
            @RequestHeader("X-Organization-ID") String organizationId) {
        LeadService.LeadAnalytics analytics = leadService.getLeadAnalytics(organizationId);
        return ResponseEntity.ok(analytics);
    }
}
