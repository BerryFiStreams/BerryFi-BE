package com.berryfi.portal.service;

import com.berryfi.portal.dto.projectshare.*;
import com.berryfi.portal.entity.Organization;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.entity.ProjectShare;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.ProjectShareStatus;
import com.berryfi.portal.enums.ShareType;
import com.berryfi.portal.exception.ResourceNotFoundException;
import com.berryfi.portal.repository.OrganizationRepository;
import com.berryfi.portal.repository.ProjectRepository;
import com.berryfi.portal.repository.ProjectShareRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing project VM access sharing between organizations.
 * 
 * Project sharing allows organizations to give other organizations access to VMs
 * within their Azure deployed projects through trackable links. The owner organization
 * can monitor usage lifecycle and credit consumption follows: gifted credits first,
 * then project-level credits allocated by the organization.
 */
@Service
@Transactional
public class ProjectShareService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectShareService.class);

    @Autowired
    private ProjectShareRepository projectShareRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    /**
     * Share project VM access with another organization.
     * This creates a trackable link for the target organization to access VMs
     * within the project, along with optional credit allocation.
     */
    @PreAuthorize("hasPermission('project', 'share')")
    public ProjectShareResponse shareProject(CreateProjectShareRequest request, User currentUser) {
        logger.info("Sharing project VM access {} with organization {} by user {}", 
                   request.getProjectId(), request.getTargetOrganizationId(), currentUser.getId());

        // Validate project exists and user has access
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOrganizationId().equals(currentUser.getOrganizationId())) {
            throw new RuntimeException("You can only share VM access for projects from your organization");
        }

        // Validate target organization
        Organization targetOrganization = organizationRepository.findById(request.getTargetOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Target organization not found"));

        if (!targetOrganization.getCanReceiveSharedProjects()) {
            throw new RuntimeException("Target organization cannot receive shared projects");
        }

        // Check for existing VM access share
        if (projectShareRepository.existsByProjectIdAndSharedWithOrganizationIdAndStatusIn(
                request.getProjectId(), request.getTargetOrganizationId(), 
                List.of(ProjectShareStatus.PENDING, ProjectShareStatus.ACCEPTED))) {
            throw new RuntimeException("Project VM access is already shared with this organization");
        }

        // Get sharing organization
        Organization sharingOrganization = organizationRepository.findById(currentUser.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("User's organization not found"));

        if (!sharingOrganization.getCanShareProjects()) {
            throw new RuntimeException("Your organization cannot share project VM access");
        }

        // Create project VM access share with trackable link
        ProjectShare projectShare = new ProjectShare(
            request.getProjectId(),
            currentUser.getOrganizationId(),
            request.getTargetOrganizationId(),
            request.getShareType() != null ? request.getShareType() : ShareType.ONE_TIME,
            currentUser.getId()
        );

        // Set additional fields if available
        if (request.getCreditsAllocated() != null && request.getCreditsAllocated() > 0) {
            projectShare.setAllocatedCredits(request.getCreditsAllocated());
        }

        if (request.getExpiresAt() != null) {
            projectShare.setExpiresAt(request.getExpiresAt());
        }

        if (request.getRecurringDays() != null && request.getRecurringDays() > 0) {
            projectShare.setRecurringIntervalDays(request.getRecurringDays());
        }

        // Note: message field not available in current entity structure

        ProjectShare savedShare = projectShareRepository.save(projectShare);
        logger.info("Created project VM access share with ID: {} - trackable link generated for organization: {}", 
                   savedShare.getId(), request.getTargetOrganizationId());
        
        return mapToResponse(savedShare);
    }

    /**
     * Accept project VM access sharing.
     * This enables the organization to access VMs in the shared project.
     */
    @PreAuthorize("hasPermission('project', 'accept_share')")
    public ProjectShareResponse acceptProjectShare(String shareId, User currentUser) {
        logger.info("Accepting project share {} by user {}", shareId, currentUser.getId());

        ProjectShare projectShare = projectShareRepository.findById(shareId)
            .orElseThrow(() -> new ResourceNotFoundException("Project share not found"));

        if (!projectShare.getSharedWithOrganizationId().equals(currentUser.getOrganizationId())) {
            throw new RuntimeException("You can only accept VM access shares for your organization");
        }

        if (projectShare.getStatus() != ProjectShareStatus.PENDING) {
            throw new RuntimeException("Project VM access share is not in pending status");
        }

        if (projectShare.isExpired()) {
            throw new RuntimeException("Project VM access share has expired");
        }

        projectShare.acceptShare(currentUser.getId());
        ProjectShare updatedShare = projectShareRepository.save(projectShare);
        
        logger.info("Accepted project VM access share: {} - organization can now access VMs", shareId);
        return mapToResponse(updatedShare);
    }

    /**
     * Reject project VM access sharing.
     */
    @PreAuthorize("hasPermission('project', 'reject_share')")
    public ProjectShareResponse rejectProjectShare(String shareId, String reason, User currentUser) {
        logger.info("Rejecting project share {} by user {}", shareId, currentUser.getId());

        ProjectShare projectShare = projectShareRepository.findById(shareId)
            .orElseThrow(() -> new ResourceNotFoundException("Project share not found"));

        if (!projectShare.getSharedWithOrganizationId().equals(currentUser.getOrganizationId())) {
            throw new RuntimeException("You can only reject shares for your organization");
        }

        if (projectShare.getStatus() != ProjectShareStatus.PENDING) {
            throw new RuntimeException("Project share is not in pending status");
        }

        projectShare.rejectShare();
        ProjectShare updatedShare = projectShareRepository.save(projectShare);
        
        logger.info("Rejected project share: {}", shareId);
        return mapToResponse(updatedShare);
    }

    /**
     * Revoke a project share.
     */
    @PreAuthorize("hasPermission('project', 'revoke_share')")
    public void revokeProjectShare(String shareId, String reason, User currentUser) {
        logger.info("Revoking project share {} by user {}", shareId, currentUser.getId());

        ProjectShare projectShare = projectShareRepository.findById(shareId)
            .orElseThrow(() -> new ResourceNotFoundException("Project share not found"));

        if (!projectShare.getSharedByOrganizationId().equals(currentUser.getOrganizationId())) {
            throw new RuntimeException("You can only revoke shares from your organization");
        }

        projectShare.revokeShare();
        projectShareRepository.save(projectShare);
        
        logger.info("Revoked project share: {}", shareId);
    }

    /**
     * Get project share by ID.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public ProjectShareResponse getProjectShare(String shareId, User currentUser) {
        ProjectShare projectShare = projectShareRepository.findById(shareId)
            .orElseThrow(() -> new ResourceNotFoundException("Project share not found"));

        // Check access
        if (!projectShare.getSharedByOrganizationId().equals(currentUser.getOrganizationId()) &&
            !projectShare.getSharedWithOrganizationId().equals(currentUser.getOrganizationId())) {
            throw new RuntimeException("Access denied to project share");
        }

        return mapToResponse(projectShare);
    }

    /**
     * Get shared project VM access for organization (VM access shared TO this organization).
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public Page<ProjectShareResponse> getSharedProjects(String organizationId, User currentUser, Pageable pageable) {
        if (!organizationId.equals(currentUser.getOrganizationId())) {
            throw new RuntimeException("Access denied to organization's shared project VM access");
        }

        Page<ProjectShare> shares = projectShareRepository.findBySharedWithOrganizationId(organizationId, pageable);
        return shares.map(this::mapToResponse);
    }

    /**
     * Get outgoing VM access shares for organization (project VM access shared BY this organization).
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public Page<ProjectShareResponse> getOutgoingShares(String organizationId, User currentUser, Pageable pageable) {
        if (!organizationId.equals(currentUser.getOrganizationId())) {
            throw new RuntimeException("Access denied to organization's outgoing VM access shares");
        }

        Page<ProjectShare> shares = projectShareRepository.findBySharedByOrganizationId(organizationId, pageable);
        return shares.map(this::mapToResponse);
    }

    /**
     * Get project shares by status.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public Page<ProjectShareResponse> getProjectSharesByStatus(String organizationId, ProjectShareStatus status, 
                                                              User currentUser, Pageable pageable) {
        if (!organizationId.equals(currentUser.getOrganizationId())) {
            throw new RuntimeException("Access denied to organization's project shares");
        }

        Page<ProjectShare> shares = projectShareRepository.findBySharedWithOrganizationIdAndStatus(
            organizationId, status, pageable);
        return shares.map(this::mapToResponse);
    }

    /**
     * Use credits for VM sessions in shared project.
     * Credits are consumed in order: gifted credits first, then project-level credits.
     */
    public void useSharedProjectCredits(String shareId, double creditsUsed) {
        logger.debug("Using {} credits for VM sessions in shared project {}", creditsUsed, shareId);

        ProjectShare projectShare = projectShareRepository.findById(shareId)
            .orElseThrow(() -> new ResourceNotFoundException("Project VM access share not found"));

        if (projectShare.getStatus() != ProjectShareStatus.ACCEPTED) {
            throw new RuntimeException("Project VM access share is not active");
        }

        if (projectShare.isExpired()) {
            throw new RuntimeException("Project VM access share has expired");
        }

        // Use gifted credits first (from project sharing)
        projectShare.useCredits(creditsUsed);
        projectShareRepository.save(projectShare);
        
        logger.debug("Credits consumed from shared project. Remaining gifted credits: {}", 
                    projectShare.getRemainingCredits());
        
        // If gifted credits are exhausted, project-level credits from organization will be used
        // (This would be handled by the VM session service when calling organization credit service)
    }

    /**
     * Process recurring credit gifts.
     */
    public void processRecurringCredits() {
        logger.info("Processing recurring credit gifts");
        
        List<ProjectShare> recurringShares = projectShareRepository.findSharesNeedingRecurringCredits(LocalDateTime.now());
        for (ProjectShare share : recurringShares) {
            if (share.getShareType() == ShareType.RECURRING && share.needsRecurringCreditGift()) {
                share.addRecurringCredits();
                projectShareRepository.save(share);
                logger.info("Refilled credits for recurring share: {}", share.getId());
            }
        }
    }

    /**
     * Find expired project shares.
     */
    public List<ProjectShare> findExpiredShares() {
        return projectShareRepository.findExpiredShares(LocalDateTime.now());
    }

    /**
     * Find shares with low credits.
     */
    public List<ProjectShare> findSharesWithLowCredits(double threshold) {
        // Using custom query to find shares with low remaining credits
        return projectShareRepository.findAll().stream()
            .filter(share -> share.getRemainingCredits() != null && share.getRemainingCredits() < threshold)
            .toList();
    }

    /**
     * Map ProjectShare entity to ProjectShareResponse DTO.
     */
    private ProjectShareResponse mapToResponse(ProjectShare share) {
        ProjectShareResponse response = new ProjectShareResponse();
        response.setId(share.getId());
        response.setProjectId(share.getProjectId());
        // Note: Project name would need to be fetched from Project entity
        response.setProjectName(null);
        response.setSharingOrganizationId(share.getSharedByOrganizationId());
        // Note: Organization names would need to be fetched from Organization entities
        response.setSharingOrganizationName(null);
        response.setTargetOrganizationId(share.getSharedWithOrganizationId());
        response.setTargetOrganizationName(null);
        response.setSharedById(share.getCreatedBy());
        // Note: User names would need to be fetched from User entities  
        response.setSharedByName(null);
        response.setStatus(share.getStatus());
        response.setShareType(share.getShareType());
        response.setCreditsAllocated(share.getAllocatedCredits());
        response.setCreditsUsed(share.getUsedCredits());
        response.setRemainingCredits(share.getRemainingCredits());
        response.setMessage(share.getShareMessage());
        response.setExpiresAt(share.getExpiresAt());
        response.setRecurringDays(share.getRecurringIntervalDays());
        response.setAcceptedById(share.getAcceptedBy());
        response.setAcceptedByName(null); // Would need User lookup
        response.setAcceptedAt(share.getAcceptedAt());
        response.setRejectedById(null); // Not available in entity
        response.setRejectedByName(null);
        response.setRejectedAt(share.getRejectedAt());
        response.setRejectionReason(null); // Not available in entity
        response.setRevokedById(null); // Not available in entity
        response.setRevokedByName(null);
        response.setRevokedAt(share.getRevokedAt());
        response.setRevocationReason(null); // Not available in entity
        response.setLastCreditRefillAt(share.getLastCreditGiftDate());
        response.setCreatedAt(share.getSharedAt());
        response.setUpdatedAt(share.getUpdatedAt());
        return response;
    }
}