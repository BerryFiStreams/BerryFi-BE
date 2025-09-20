package com.berryfi.portal.controller;

import com.berryfi.portal.dto.organization.*;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for organization management operations.
 */
@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Organization management operations")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    /**
     * Create a new organization.
     */
    @PostMapping
    @Operation(summary = "Create organization", 
              description = "Create a new organization. User can only own one organization.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Organization created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "User already owns an organization or name already exists")
    })
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        OrganizationResponse response = organizationService.createOrganization(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get current user's organization.
     */
    @GetMapping("/my")
    @Operation(summary = "Get my organization", 
              description = "Get the organization associated with the current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organization retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User is not associated with any organization")
    })
    public ResponseEntity<OrganizationResponse> getMyOrganization(
            @AuthenticationPrincipal User currentUser) {
        
        OrganizationResponse response = organizationService.getMyOrganization(currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Get organization by ID.
     */
    @GetMapping("/{organizationId}")
    @Operation(summary = "Get organization", 
              description = "Get organization details by ID. User must have access to the organization.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organization retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied to organization"),
        @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<OrganizationResponse> getOrganization(
            @Parameter(description = "Organization ID") @PathVariable String organizationId,
            @AuthenticationPrincipal User currentUser) {
        
        OrganizationResponse response = organizationService.getOrganization(organizationId, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Update organization.
     */
    @PutMapping("/{organizationId}")
    @Operation(summary = "Update organization", 
              description = "Update organization details. Only organization owner can update details.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organization updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Only organization owner can update details"),
        @ApiResponse(responseCode = "404", description = "Organization not found"),
        @ApiResponse(responseCode = "409", description = "Organization name already exists")
    })
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @Parameter(description = "Organization ID") @PathVariable String organizationId,
            @Valid @RequestBody UpdateOrganizationRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        OrganizationResponse response = organizationService.updateOrganization(organizationId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Add credits to organization.
     */
    @PostMapping("/{organizationId}/credits")
    @Operation(summary = "Add credits", 
              description = "Add credits to organization.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Credits added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied to organization"),
        @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<OrganizationResponse> addCredits(
            @Parameter(description = "Organization ID") @PathVariable String organizationId,
            @Valid @RequestBody AddCreditsRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        OrganizationResponse response = organizationService.addCredits(organizationId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Get organization statistics.
     */
    @GetMapping("/{organizationId}/stats")
    @Operation(summary = "Get organization statistics", 
              description = "Get detailed statistics for organization including projects, members, credits usage.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied to organization"),
        @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<OrganizationStatsResponse> getOrganizationStats(
            @Parameter(description = "Organization ID") @PathVariable String organizationId,
            @AuthenticationPrincipal User currentUser) {
        
        OrganizationStatsResponse response = organizationService.getOrganizationStats(organizationId, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Search organizations.
     */
    @GetMapping("/search")
    @Operation(summary = "Search organizations", 
              description = "Search organizations by name keyword.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    public ResponseEntity<Page<OrganizationSummary>> searchOrganizations(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<OrganizationSummary> response = organizationService.searchOrganizations(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get organizations that can share projects.
     */
    @GetMapping("/can-share")
    @Operation(summary = "Get organizations that can share", 
              description = "Get list of organizations that can share projects with others.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organizations retrieved successfully")
    })
    public ResponseEntity<Page<OrganizationSummary>> getOrganizationsThatCanShare(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<OrganizationSummary> response = organizationService.getOrganizationsThatCanShare(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get organizations that can receive shared projects.
     */
    @GetMapping("/can-receive-shares")
    @Operation(summary = "Get organizations that can receive shares", 
              description = "Get list of organizations that can receive shared projects from others.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organizations retrieved successfully")
    })
    public ResponseEntity<Page<OrganizationSummary>> getOrganizationsThatCanReceiveShares(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<OrganizationSummary> response = organizationService.getOrganizationsThatCanReceiveShares(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Suspend organization (Admin only).
     */
    @PostMapping("/{organizationId}/suspend")
    @Operation(summary = "Suspend organization", 
              description = "Suspend an organization. Admin operation only.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organization suspended successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<Void> suspendOrganization(
            @Parameter(description = "Organization ID") @PathVariable String organizationId,
            @Parameter(description = "Suspension reason") @RequestParam String reason,
            @AuthenticationPrincipal User currentUser) {
        
        organizationService.suspendOrganization(organizationId, reason, currentUser);
        return ResponseEntity.ok().build();
    }

    /**
     * Activate organization (Admin only).
     */
    @PostMapping("/{organizationId}/activate")
    @Operation(summary = "Activate organization", 
              description = "Activate a suspended organization. Admin operation only.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organization activated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<Void> activateOrganization(
            @Parameter(description = "Organization ID") @PathVariable String organizationId,
            @AuthenticationPrincipal User currentUser) {
        
        organizationService.activateOrganization(organizationId, currentUser);
        return ResponseEntity.ok().build();
    }
}