package com.berryfi.portal.service;

import com.berryfi.portal.dto.organization.*;
import com.berryfi.portal.entity.Organization;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.OrganizationStatus;
import com.berryfi.portal.exception.ResourceNotFoundException;
import com.berryfi.portal.repository.OrganizationRepository;
import com.berryfi.portal.repository.ProjectRepository;
import com.berryfi.portal.repository.TeamMemberRepository;
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
 * Service class for managing organizations.
 */
@Service
@Transactional
public class OrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private BillingService billingService;

    /**
     * Create a new organization.
     */
    @PreAuthorize("hasPermission('organization', 'create')")
    public OrganizationResponse createOrganization(CreateOrganizationRequest request, User currentUser) {
        logger.info("Creating organization: {} for user: {}", request.getName(), currentUser.getId());

        // Check if user already owns an organization
        if (organizationRepository.existsByOwnerEmail(currentUser.getEmail())) {
            throw new RuntimeException("User already owns an organization");
        }

        // Check if organization name is unique
        if (organizationRepository.existsByName(request.getName())) {
            throw new RuntimeException("Organization name already exists");
        }

        Organization organization = new Organization(
            request.getName(),
            request.getDescription(),
            currentUser.getId(),
            currentUser.getEmail(),
            currentUser.getName(),
            currentUser.getId()
        );

        // Set initial settings
        if (request.getMaxProjects() != null) {
            organization.setMaxProjects(request.getMaxProjects());
        }
        if (request.getMaxMembers() != null) {
            organization.setMaxMembers(request.getMaxMembers());
        }
        if (request.getMonthlyBudget() != null) {
            organization.setMonthlyBudget(request.getMonthlyBudget());
        }

        organization.setCanShareProjects(request.getCanShareProjects() != null ? request.getCanShareProjects() : true);
        organization.setCanReceiveSharedProjects(request.getCanReceiveSharedProjects() != null ? request.getCanReceiveSharedProjects() : true);

        Organization savedOrganization = organizationRepository.save(organization);

        // Update user's organization ID
        currentUser.setOrganizationId(savedOrganization.getId());

        logger.info("Created organization: {} with ID: {}", savedOrganization.getName(), savedOrganization.getId());
        return mapToResponse(savedOrganization);
    }

    /**
     * Get organization by ID.
     */
    @PreAuthorize("hasPermission('organization', 'read')")
    public OrganizationResponse getOrganization(String organizationId, User currentUser) {
        logger.debug("Fetching organization: {}", organizationId);

        Organization organization = findOrganizationWithAccess(organizationId, currentUser);
        return mapToResponse(organization);
    }

    /**
     * Get current user's organization.
     */
    @PreAuthorize("hasPermission('organization', 'read')")
    public OrganizationResponse getMyOrganization(User currentUser) {
        if (currentUser.getOrganizationId() == null) {
            throw new ResourceNotFoundException("User is not associated with any organization");
        }
        return getOrganization(currentUser.getOrganizationId(), currentUser);
    }

    /**
     * Update organization.
     */
    @PreAuthorize("hasPermission('organization', 'update')")
    public OrganizationResponse updateOrganization(String organizationId, UpdateOrganizationRequest request, User currentUser) {
        logger.info("Updating organization: {} by user: {}", organizationId, currentUser.getId());

        Organization organization = findOrganizationWithAccess(organizationId, currentUser);

        // Only owner can update organization details
        if (!organization.getOwnerId().equals(currentUser.getId())) {
            throw new RuntimeException("Only organization owner can update organization details");
        }

        if (request.getName() != null && !request.getName().equals(organization.getName())) {
            if (organizationRepository.existsByName(request.getName())) {
                throw new RuntimeException("Organization name already exists");
            }
            organization.setName(request.getName());
        }

        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }

        if (request.getMaxProjects() != null) {
            organization.setMaxProjects(request.getMaxProjects());
        }

        if (request.getMaxMembers() != null) {
            organization.setMaxMembers(request.getMaxMembers());
        }

        if (request.getMonthlyBudget() != null) {
            organization.setMonthlyBudget(request.getMonthlyBudget());
        }

        if (request.getCanShareProjects() != null) {
            organization.setCanShareProjects(request.getCanShareProjects());
        }

        if (request.getCanReceiveSharedProjects() != null) {
            organization.setCanReceiveSharedProjects(request.getCanReceiveSharedProjects());
        }

        Organization updatedOrganization = organizationRepository.save(organization);
        logger.info("Updated organization: {}", updatedOrganization.getId());
        return mapToResponse(updatedOrganization);
    }

    /**
     * Add credits to organization.
     */
    @PreAuthorize("hasPermission('organization', 'manage_credits')")
    public OrganizationResponse addCredits(String organizationId, AddCreditsRequest request, User currentUser) {
        logger.info("Adding {} credits to organization: {} (purchased: {})", 
                request.getCredits(), organizationId, request.isPurchased());

        Organization organization = findOrganizationWithAccess(organizationId, currentUser);

        // Update organization credits tracking
        organization.addCredits(request.getCredits(), request.isPurchased());
        Organization updatedOrganization = organizationRepository.save(organization);
        
        // IMPORTANT: Also create billing transaction for credit tracking system
        String description = request.isPurchased() 
            ? String.format("Purchased credits: %.2f credits", request.getCredits())
            : String.format("Gifted credits: %.2f credits", request.getCredits());
        
        try {
            billingService.addCredits(organizationId, request.getCredits(), description);
            logger.info("Created billing transaction for {} credits to organization: {}", 
                    request.getCredits(), organizationId);
        } catch (Exception e) {
            logger.error("Failed to create billing transaction, but organization credits updated", e);
            // Continue - organization credits are already updated
        }
        
        logger.info("Successfully added {} credits to organization: {}", request.getCredits(), organizationId);
        return mapToResponse(updatedOrganization);
    }

    /**
     * Get organization statistics.
     */
    @PreAuthorize("hasPermission('organization', 'read')")
    public OrganizationStatsResponse getOrganizationStats(String organizationId, User currentUser) {
        logger.debug("Fetching stats for organization: {}", organizationId);

        Organization organization = findOrganizationWithAccess(organizationId, currentUser);
        
        OrganizationStatsResponse stats = new OrganizationStatsResponse();
        stats.setOrganizationId(organizationId);
        stats.setTotalProjects(organization.getActiveProjects());
        stats.setTotalMembers(organization.getTotalMembers());
        stats.setTotalSessions(organization.getTotalSessions());
        stats.setTotalCredits(organization.getTotalCredits());
        stats.setUsedCredits(organization.getUsedCredits());
        stats.setRemainingCredits(organization.getRemainingCredits());
        stats.setMonthlyCreditsUsed(organization.getMonthlyCreditsUsed());
        stats.setMonthlyBudget(organization.getMonthlyBudget());
        stats.setIsOverBudget(organization.isOverBudget());

        // Get project count from repository
        long projectCount = projectRepository.countByOrganizationId(organizationId);
        stats.setTotalProjects((int) projectCount);

        // Get member count from repository
        long memberCount = teamMemberRepository.countByOrganizationId(organizationId);
        stats.setTotalMembers((int) memberCount);

        return stats;
    }

    /**
     * Search organizations.
     */
    @PreAuthorize("hasPermission('organization', 'search')")
    public Page<OrganizationSummary> searchOrganizations(String keyword, Pageable pageable) {
        logger.debug("Searching organizations with keyword: {}", keyword);

        Page<Organization> organizations = organizationRepository.searchByName(keyword, pageable);
        return organizations.map(this::mapToSummary);
    }

    /**
     * Get organizations that can share projects.
     */
    @PreAuthorize("hasPermission('organization', 'read')")
    public Page<OrganizationSummary> getOrganizationsThatCanShare(Pageable pageable) {
        Page<Organization> organizations = organizationRepository.findOrganizationsThatCanShare(pageable);
        return organizations.map(this::mapToSummary);
    }

    /**
     * Get organizations that can receive shared projects.
     */
    @PreAuthorize("hasPermission('organization', 'read')")
    public Page<OrganizationSummary> getOrganizationsThatCanReceiveShares(Pageable pageable) {
        Page<Organization> organizations = organizationRepository.findOrganizationsThatCanReceiveShares(pageable);
        return organizations.map(this::mapToSummary);
    }

    /**
     * Suspend organization.
     */
    @PreAuthorize("hasPermission('organization', 'suspend')")
    public void suspendOrganization(String organizationId, String reason, User currentUser) {
        logger.warn("Suspending organization: {} by user: {} for reason: {}", 
                   organizationId, currentUser.getId(), reason);

        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        organization.setStatus(OrganizationStatus.SUSPENDED);
        organizationRepository.save(organization);
    }

    /**
     * Activate organization.
     */
    @PreAuthorize("hasPermission('organization', 'activate')")
    public void activateOrganization(String organizationId, User currentUser) {
        logger.info("Activating organization: {} by user: {}", organizationId, currentUser.getId());

        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        organization.setStatus(OrganizationStatus.ACTIVE);
        organizationRepository.save(organization);
    }

    /**
     * Use credits for organization.
     */
    public void useCredits(String organizationId, double creditsUsed) {
        logger.debug("Using {} credits for organization: {}", creditsUsed, organizationId);

        organizationRepository.updateCreditsUsed(organizationId, creditsUsed, LocalDateTime.now());
        
        // Increment session count
        organizationRepository.incrementSessionCount(organizationId, LocalDateTime.now());
    }

    /**
     * Reset monthly usage for all organizations.
     */
    public void resetMonthlyUsage() {
        logger.info("Resetting monthly usage for all organizations");
        organizationRepository.resetMonthlyUsage(LocalDateTime.now());
    }

    /**
     * Find organizations with low credits.
     */
    public List<Organization> findOrganizationsWithLowCredits(double threshold) {
        return organizationRepository.findOrganizationsWithLowCredits(threshold);
    }

    /**
     * Find organizations over budget.
     */
    public List<Organization> findOrganizationsOverBudget() {
        return organizationRepository.findOrganizationsOverBudget();
    }

    /**
     * Helper method to find organization with access check.
     */
    private Organization findOrganizationWithAccess(String organizationId, User currentUser) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        // Check if user has access to this organization
        if (!hasAccessToOrganization(currentUser, organizationId)) {
            throw new RuntimeException("Access denied to organization");
        }

        return organization;
    }

    /**
     * Check if user has access to organization.
     */
    private boolean hasAccessToOrganization(User user, String organizationId) {
        return user.getOrganizationId() != null && user.getOrganizationId().equals(organizationId);
    }

    /**
     * Map Organization entity to OrganizationResponse DTO.
     */
    private OrganizationResponse mapToResponse(Organization organization) {
        OrganizationResponse response = new OrganizationResponse();
        response.setId(organization.getId());
        response.setName(organization.getName());
        response.setDescription(organization.getDescription());
        response.setOwnerId(organization.getOwnerId());
        response.setOwnerEmail(organization.getOwnerEmail());
        response.setOwnerName(organization.getOwnerName());
        response.setStatus(organization.getStatus());
        response.setTotalCredits(organization.getTotalCredits());
        response.setUsedCredits(organization.getUsedCredits());
        response.setRemainingCredits(organization.getRemainingCredits());
        response.setPurchasedCredits(organization.getPurchasedCredits());
        response.setGiftedCredits(organization.getGiftedCredits());
        response.setMonthlyBudget(organization.getMonthlyBudget());
        response.setMonthlyCreditsUsed(organization.getMonthlyCreditsUsed());
        response.setCanShareProjects(organization.getCanShareProjects());
        response.setCanReceiveSharedProjects(organization.getCanReceiveSharedProjects());
        response.setMaxProjects(organization.getMaxProjects());
        response.setMaxMembers(organization.getMaxMembers());
        response.setActiveProjects(organization.getActiveProjects());
        response.setTotalMembers(organization.getTotalMembers());
        response.setTotalSessions(organization.getTotalSessions());
        response.setCreatedAt(organization.getCreatedAt());
        response.setUpdatedAt(organization.getUpdatedAt());
        return response;
    }

    /**
     * Map Organization entity to OrganizationSummary DTO.
     */
    private OrganizationSummary mapToSummary(Organization organization) {
        OrganizationSummary summary = new OrganizationSummary();
        summary.setId(organization.getId());
        summary.setName(organization.getName());
        summary.setDescription(organization.getDescription());
        summary.setOwnerName(organization.getOwnerName());
        summary.setStatus(organization.getStatus());
        summary.setActiveProjects(organization.getActiveProjects());
        summary.setTotalMembers(organization.getTotalMembers());
        summary.setCanShareProjects(organization.getCanShareProjects());
        summary.setCanReceiveSharedProjects(organization.getCanReceiveSharedProjects());
        summary.setCreatedAt(organization.getCreatedAt());
        return summary;
    }
}