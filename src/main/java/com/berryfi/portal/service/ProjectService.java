package com.berryfi.portal.service;

import com.berryfi.portal.dto.project.*;
import com.berryfi.portal.entity.Project;
import com.berryfi.portal.entity.ProjectShare;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.entity.Organization;

import com.berryfi.portal.enums.ProjectStatus;
import com.berryfi.portal.enums.ProjectShareStatus;
import com.berryfi.portal.enums.ShareType;
import com.berryfi.portal.exception.ResourceNotFoundException;
import com.berryfi.portal.repository.ProjectRepository;
import com.berryfi.portal.repository.ProjectShareRepository;
import com.berryfi.portal.repository.VmSessionRepository;
import com.berryfi.portal.repository.OrganizationRepository;
import com.berryfi.portal.repository.UserRepository;
import com.berryfi.portal.repository.ProjectInvitationRepository;
import com.berryfi.portal.entity.ProjectInvitation;
import com.berryfi.portal.enums.InvitationStatus;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for managing projects.
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private UrlTrackingService urlTrackingService;

    @Autowired
    private VmSessionRepository vmSessionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectShareRepository projectShareRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectInvitationRepository projectInvitationRepository;

    @Autowired
    private InvitationEmailService invitationEmailService;

    /**
     * Create a new project.
     */
    @PreAuthorize("hasPermission('project', 'create')")
    public ProjectResponse createProject(CreateProjectRequest request, User currentUser) {
        String organizationId = currentUser.getOrganizationId();
        logger.info("Creating new project: {} for organization: {}", request.getName(), organizationId);

        // Check if project name already exists in organization
        if (projectRepository.existsByNameAndOrganizationId(request.getName(), organizationId)) {
            throw new IllegalArgumentException("Project with name '" + request.getName() + "' already exists in this organization");
        }

        Project project = new Project(
            request.getName(),
            request.getDescription(),
            organizationId,
            request.getAccountType(),
            currentUser.getId()
        );

        project.setConfig(request.getConfig());
        project.setBranding(request.getBranding());
        project.setLinks(request.getLinks());

        // Auto-generate unique subdomain for multi-tenant support
        String subdomain = tenantService.generateUniqueSubdomain(request.getName());
        project.setSubdomain(subdomain);

        Project savedProject = projectRepository.save(project);
        logger.info("Created project: {} with ID: {} and subdomain: {}", 
                   savedProject.getName(), savedProject.getId(), subdomain);

        return ProjectResponse.from(savedProject);
    }

    /**
     * Get all projects for an organization with pagination.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public Page<ProjectSummary> getProjects(String organizationId, Pageable pageable, User currentUser) {
        logger.debug("Fetching projects for organization: {}", organizationId);

        // Ensure user has access to this organization
        validateOrganizationAccess(organizationId, currentUser);

        return projectRepository.findOwnedAndSharedProjects(organizationId, pageable)
                .map(project -> {
                    ProjectSummary summary = ProjectSummary.from(project);
                    
                    // Set access type and sharing information
                    if (project.getOrganizationId().equals(organizationId)) {
                        summary.setAccessType("OWNED");
                        summary.setSharedBy(null);
                    } else {
                        summary.setAccessType("SHARED");
                        // Find who directly shared this project with the current organization
                        Optional<ProjectShare> directShare = projectShareRepository
                            .findByProjectIdAndSharedWithOrganizationIdAndStatus(
                                project.getId(), organizationId, ProjectShareStatus.ACCEPTED);
                        if (directShare.isPresent()) {
                            String sharedByOrgName = getOrganizationName(directShare.get().getSharedByOrganizationId());
                            summary.setSharedBy(sharedByOrgName);
                        } else {
                            summary.setSharedBy("Unknown");
                        }
                    }
                    
                    // Calculate real statistics from VM sessions
                    String projectId = project.getId();
                    Double totalCreditsUsed = vmSessionRepository.getTotalCreditsUsedByProjectAllTime(projectId);
                    Long sessionsCount = vmSessionRepository.countSessionsByProject(projectId);
                    Long totalDurationSeconds = vmSessionRepository.getTotalDurationSecondsByProject(projectId);
                    
                    // Calculate uptime as total duration in hours
                    Double uptime = totalDurationSeconds != null ? totalDurationSeconds / 3600.0 : 0.0;
                    
                    // Override with real calculated statistics
                    summary.setTotalCreditsUsed(totalCreditsUsed != null ? totalCreditsUsed : 0.0);
                    summary.setSessionsCount(sessionsCount != null ? sessionsCount.intValue() : 0);
                    summary.setUptime(uptime);
                    
                    // Generate user-specific tracking URL using project's subdomain or custom domain
                    if (project.getLinks() != null && !project.getLinks().trim().isEmpty()) {
                        String trackingUrl = urlTrackingService.generateTrackingUrl(
                            project, currentUser.getId());
                        summary.setTrackingUrl(trackingUrl);
                    }
                    return summary;
                });
    }

    /**
     * Get a specific project by ID.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public ProjectResponse getProject(String projectId, User currentUser) {
        logger.debug("Fetching project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);
        
        // Calculate real statistics from VM sessions
        Double totalCreditsUsed = vmSessionRepository.getTotalCreditsUsedByProjectAllTime(projectId);
        Long sessionsCount = vmSessionRepository.countSessionsByProject(projectId);
        Long totalDurationSeconds = vmSessionRepository.getTotalDurationSecondsByProject(projectId);
        
        // Calculate uptime as a percentage (total duration / total possible time)
        // For this example, we'll calculate uptime as total duration in hours
        Double uptime = totalDurationSeconds != null ? totalDurationSeconds / 3600.0 : 0.0;
        
        // Create response from project entity
        ProjectResponse response = ProjectResponse.from(project);
        
        // Set access type and sharing information
        String organizationId = currentUser.getOrganizationId();
        if (project.getOrganizationId().equals(organizationId)) {
            response.setAccessType("OWNED");
            response.setSharedBy(null);
        } else {
            response.setAccessType("SHARED");
            // Find who directly shared this project with the current organization
            Optional<ProjectShare> directShare = projectShareRepository
                .findByProjectIdAndSharedWithOrganizationIdAndStatus(
                    projectId, organizationId, ProjectShareStatus.ACCEPTED);
            if (directShare.isPresent()) {
                String sharedByOrgName = getOrganizationName(directShare.get().getSharedByOrganizationId());
                response.setSharedBy(sharedByOrgName);
            } else {
                response.setSharedBy("Unknown");
            }
        }
        
        // Override with real calculated statistics
        response.setTotalCreditsUsed(totalCreditsUsed != null ? totalCreditsUsed : 0.0);
        response.setSessionsCount(sessionsCount != null ? sessionsCount.intValue() : 0);
        response.setUptime(uptime);
        
        logger.debug("Project {} statistics: credits={}, sessions={}, uptime={}", 
                    projectId, totalCreditsUsed, sessionsCount, uptime);
        
        return response;
    }

    /**
     * Update a project.
     */
    @PreAuthorize("hasPermission('project', 'update')")
    public ProjectResponse updateProject(String projectId, UpdateProjectRequest request, User currentUser) {
        logger.info("Updating project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            // Check if new name already exists (excluding current project)
            if (!request.getName().equals(project.getName()) && 
                projectRepository.existsByNameAndOrganizationId(request.getName(), project.getOrganizationId())) {
                throw new IllegalArgumentException("Project with name '" + request.getName() + "' already exists in this organization");
            }
            project.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        if (request.getConfig() != null) {
            project.setConfig(request.getConfig());
        }

        if (request.getBranding() != null) {
            project.setBranding(request.getBranding());
        }

        if (request.getLinks() != null) {
            project.setLinks(request.getLinks());
        }

        // Handle tenant configuration
        if (request.getTenantConfig() != null) {
            ProjectTenantConfigDTO tenantConfigDTO = request.getTenantConfig();
            
            // Update subdomain
            if (tenantConfigDTO.getSubdomain() != null && !tenantConfigDTO.getSubdomain().trim().isEmpty()) {
                String newSubdomain = tenantConfigDTO.getSubdomain().trim().toLowerCase();
                
                // Check if subdomain is already taken by another project
                if (!newSubdomain.equals(project.getSubdomain())) {
                    Optional<Project> existingProject = projectRepository.findBySubdomain(newSubdomain);
                    if (existingProject.isPresent()) {
                        throw new IllegalArgumentException("Subdomain '" + newSubdomain + "' is already taken by another project");
                    }
                }
                
                project.setSubdomain(newSubdomain);
            }
            
            // Update branding information
            if (tenantConfigDTO.getBranding() != null) {
                ProjectTenantConfigDTO.TenantBranding branding = tenantConfigDTO.getBranding();
                
                if (branding.getAppName() != null) {
                    project.setBrandAppName(branding.getAppName());
                }
                if (branding.getPrimaryColor() != null) {
                    project.setBrandPrimaryColor(branding.getPrimaryColor());
                }
                if (branding.getSecondaryColor() != null) {
                    project.setBrandSecondaryColor(branding.getSecondaryColor());
                }
                if (branding.getLogoUrl() != null) {
                    project.setBrandLogoUrl(branding.getLogoUrl());
                }
                if (branding.getFaviconUrl() != null) {
                    project.setBrandFaviconUrl(branding.getFaviconUrl());
                }
            }
            
            // Update custom domain information
            if (tenantConfigDTO.getCustomDomain() != null) {
                ProjectTenantConfigDTO.CustomDomainConfig customDomain = tenantConfigDTO.getCustomDomain();
                
                if (customDomain.getDomain() != null) {
                    project.setCustomDomain(customDomain.getDomain());
                }
                if (customDomain.getVerified() != null) {
                    project.setCustomDomainVerified(customDomain.getVerified());
                }
            }
            
            logger.info("Updated tenant configuration for project: {}", projectId);
        }

        Project updatedProject = projectRepository.save(project);
        logger.info("Updated project: {}", updatedProject.getId());

        return ProjectResponse.from(updatedProject);
    }

    /**
     * Delete a project.
     */
    @PreAuthorize("hasPermission('project', 'delete')")
    public void deleteProject(String projectId, User currentUser) {
        logger.info("Deleting project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);

        // Stop project if running before deletion
        if (project.isRunning() || project.isDeploying()) {
            logger.info("Stopping project before deletion: {}", projectId);
            project.updateStatus(ProjectStatus.STOPPED);
            projectRepository.save(project);
        }

        projectRepository.delete(project);
        logger.info("Deleted project: {}", projectId);
    }

    /**
     * Deploy a project.
     */
    @PreAuthorize("hasPermission('project', 'deploy')")
    public ProjectResponse deployProject(String projectId, DeployProjectRequest request, User currentUser) {
        logger.info("Deploying project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);

        // Check if project is already deploying or running
        if (project.isDeploying() && !request.isForceDeploy()) {
            throw new IllegalStateException("Project is already being deployed. Use force deploy to override.");
        }

        // Update project configuration if provided
        if (request.getConfig() != null) {
            project.setConfig(request.getConfig());
        }
        if (request.getBranding() != null) {
            project.setBranding(request.getBranding());
        }
        if (request.getLinks() != null) {
            project.setLinks(request.getLinks());
        }

        // Set status to deploying
        project.updateStatus(ProjectStatus.DEPLOYING);
        project.setErrors(null); // Clear previous errors

        Project savedProject = projectRepository.save(project);

        // Start async deployment process
        deployProjectAsync(savedProject.getId());

        logger.info("Started deployment for project: {}", projectId);
        return ProjectResponse.from(savedProject);
    }

    /**
     * Stop a project.
     */
    @PreAuthorize("hasPermission('project', 'deploy')")
    public ProjectResponse stopProject(String projectId, User currentUser) {
        logger.info("Stopping project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);

        if (project.isStopped()) {
            throw new IllegalStateException("Project is already stopped");
        }

        project.updateStatus(ProjectStatus.STOPPED);
        project.setCurrentCCU(0);

        Project savedProject = projectRepository.save(project);
        logger.info("Stopped project: {}", projectId);

        return ProjectResponse.from(savedProject);
    }

    /**
     * Get project status.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public ProjectStatus getProjectStatus(String projectId, User currentUser) {
        logger.debug("Getting status for project: {}", projectId);

        Project project = findProjectWithAccess(projectId, currentUser);
        return project.getStatus();
    }

    /**
     * Search projects by keyword.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public Page<ProjectSummary> searchProjects(String organizationId, String keyword, Pageable pageable, User currentUser) {
        logger.debug("Searching projects in organization: {} with keyword: {}", organizationId, keyword);

        validateOrganizationAccess(organizationId, currentUser);

        return projectRepository.searchProjects(organizationId, keyword, pageable)
                .map(project -> {
                    ProjectSummary summary = ProjectSummary.from(project);
                    
                    // Calculate real statistics from VM sessions
                    String projectId = project.getId();
                    Double totalCreditsUsed = vmSessionRepository.getTotalCreditsUsedByProjectAllTime(projectId);
                    Long sessionsCount = vmSessionRepository.countSessionsByProject(projectId);
                    Long totalDurationSeconds = vmSessionRepository.getTotalDurationSecondsByProject(projectId);
                    
                    // Calculate uptime as total duration in hours
                    Double uptime = totalDurationSeconds != null ? totalDurationSeconds / 3600.0 : 0.0;
                    
                    // Override with real calculated statistics
                    summary.setTotalCreditsUsed(totalCreditsUsed != null ? totalCreditsUsed : 0.0);
                    summary.setSessionsCount(sessionsCount != null ? sessionsCount.intValue() : 0);
                    summary.setUptime(uptime);
                    
                    // Generate user-specific tracking URL using project's subdomain or custom domain
                    if (project.getLinks() != null && !project.getLinks().trim().isEmpty()) {
                        String trackingUrl = urlTrackingService.generateTrackingUrl(
                            project, currentUser.getId());
                        summary.setTrackingUrl(trackingUrl);
                    }
                    return summary;
                });
    }

    /**
     * Get project statistics for an organization.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public ProjectStatistics getProjectStatistics(String organizationId, User currentUser) {
        logger.debug("Getting project statistics for organization: {}", organizationId);

        validateOrganizationAccess(organizationId, currentUser);

        long totalProjects = projectRepository.countByOrganizationId(organizationId);
        long runningProjects = projectRepository.countByOrganizationIdAndStatus(organizationId, ProjectStatus.RUNNING);
        long stoppedProjects = projectRepository.countByOrganizationIdAndStatus(organizationId, ProjectStatus.STOPPED);
        long deployingProjects = projectRepository.countByOrganizationIdAndStatus(organizationId, ProjectStatus.DEPLOYING);
        long errorProjects = projectRepository.countByOrganizationIdAndStatus(organizationId, ProjectStatus.ERROR);

        Double totalCreditsUsed = projectRepository.getTotalCreditsUsedByOrganization(organizationId);
        Long totalSessions = projectRepository.getTotalSessionsCountByOrganization(organizationId);

        return new ProjectStatistics(
            totalProjects,
            runningProjects,
            stoppedProjects,
            deployingProjects,
            errorProjects,
            totalCreditsUsed != null ? totalCreditsUsed : 0.0,
            totalSessions != null ? totalSessions : 0L
        );
    }

    /**
     * Async deployment simulation (in real implementation, this would interact with deployment service).
     */
    @Transactional
    public void deployProjectAsync(String projectId) {
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate deployment time
                Thread.sleep(5000);

                Project project = projectRepository.findById(projectId).orElse(null);
                if (project != null && project.getStatus() == ProjectStatus.DEPLOYING) {
                    // Simulate successful deployment
                    project.updateStatus(ProjectStatus.RUNNING);
                    project.setLastDeployed(LocalDateTime.now());
                    projectRepository.save(project);

                    logger.info("Project deployed successfully: {}", projectId);
                }
            } catch (Exception e) {
                logger.error("Deployment failed for project: {}", projectId, e);
                
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project != null) {
                    project.updateStatus(ProjectStatus.ERROR);
                    project.setErrors("Deployment failed: " + e.getMessage());
                    projectRepository.save(project);
                }
            }
        });
    }

    /**
     * Find project with access validation (supports owned and shared projects).
     */
    private Project findProjectWithAccess(String projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        String userOrgId = currentUser.getOrganizationId();
        
        // Check if user owns the project
        if (project.getOrganizationId().equals(userOrgId)) {
            return project;
        }
        
        // Check if project is shared with user's organization
        if (project.isSharedWithOrganization(userOrgId)) {
            return project;
        }
        
        // No access - throw exception
        throw new ResourceNotFoundException("Project not found or access denied: " + projectId);
    }




    /**
     * Get project configuration.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public ProjectConfigResponse getProjectConfig(String projectId, User currentUser) {
        logger.debug("Getting configuration for project: {}", projectId);
        
        Project project = findProjectWithAccess(projectId, currentUser);
        
        // Mock implementation - return success response
        return new ProjectConfigResponse();
    }

    /**
     * Get project branding settings.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public ProjectBrandingResponse getProjectBranding(String projectId, User currentUser) {
        logger.debug("Getting branding settings for project: {}", projectId);
        
        Project project = findProjectWithAccess(projectId, currentUser);
        
        // Mock implementation - return success response
        return new ProjectBrandingResponse();
    }

    /**
     * Get project link settings.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public ProjectLinksResponse getProjectLinks(String projectId, User currentUser) {
        logger.debug("Getting link settings for project: {}", projectId);
        
        Project project = findProjectWithAccess(projectId, currentUser);
        
        // Mock implementation - return success response
        return new ProjectLinksResponse();
    }

    private void validateOrganizationAccess(String organizationId, User currentUser) {
        // In a real implementation, you would check if the user belongs to the organization
        // For now, we'll just log it
        logger.debug("Validating organization access for user: {} to organization: {}", 
                    currentUser.getId(), organizationId);
        
        // TODO: Implement proper organization access validation
        // This would typically check user's organization membership or workspace access
    }

    /**
     * Share a project with enhanced credit allocation and recipient resolution.
     */
    @PreAuthorize("hasPermission('project', 'share')")
    @Transactional
    public void shareProject(String projectId, ShareProjectRequest request, User currentUser) {
        logger.info("Sharing project {} with request: {}", projectId, request);

        // Check if user has permission to share projects (only ORG_ADMIN, ORG_OWNER, SUPER_ADMIN)
        if (!currentUser.getRole().canShareProjects()) {
            throw new IllegalArgumentException("Only organization admins can share projects. Your role: " + 
                currentUser.getRole().getDisplayName());
        }

        // Find the project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        String currentUserOrgId = currentUser.getOrganizationId();
        
        // Verify the user can share this project (either owner or has share permissions)
        boolean canShare = false;
        if (project.getOrganizationId().equals(currentUserOrgId)) {
            // User owns the project
            canShare = true;
        } else {
            // Check if user's org has reshare permissions for this project
            Optional<ProjectShare> reshareAccess = projectShareRepository
                .findReshareableAccess(projectId, currentUserOrgId, ProjectShareStatus.ACCEPTED);
            canShare = reshareAccess.isPresent();
        }
        
        if (!canShare) {
            throw new IllegalArgumentException("You don't have permission to share this project");
        }

        // Resolve target organization ID (may create invitation if user doesn't exist)
        String targetOrganizationId = resolveTargetOrganization(request, projectId, currentUser);
        
        // If targetOrganizationId is null, it means an invitation was sent instead
        if (targetOrganizationId == null) {
            logger.info("Invitation sent to {} for project {}", request.getUserEmail(), projectId);
            return; // Exit early - invitation process completed
        }
        
        // Verify target organization exists
        if (!organizationRepository.existsById(targetOrganizationId)) {
            throw new ResourceNotFoundException("Target organization not found: " + targetOrganizationId);
        }

        // Check if already shared with target organization
        boolean alreadyShared = projectShareRepository
            .existsByProjectIdAndSharedWithOrganizationIdAndStatus(
                projectId, targetOrganizationId, ProjectShareStatus.ACCEPTED);
        if (alreadyShared) {
            throw new IllegalArgumentException("Project is already shared with this organization");
        }

        // Validate and deduct credits from sharing organization
        validateAndDeductCredits(currentUserOrgId, request.getInitialCredits());

        // Create new share relationship
        ProjectShare projectShare = new ProjectShare(
            projectId,
            currentUserOrgId, // The current user's organization is sharing the project
            targetOrganizationId,
            ShareType.DIRECT,
            currentUser.getId()
        );
        
        // Set credit allocation
        projectShare.setAllocatedCredits(request.getInitialCredits());
        projectShare.setRemainingCredits(request.getInitialCredits());
        projectShare.setRecurringCredits(request.getMonthlyRecurringCredits());
        projectShare.setRecurringIntervalDays(30); // Monthly = 30 days
        
        // Set permissions from request
        projectShare.setCanViewAnalytics(request.getCanViewAnalytics());
        projectShare.setCanManageSessions(request.getCanManageSessions());
        projectShare.setCanShareFurther(request.getCanShareFurther());
        projectShare.setIsPermanent(request.getIsPermanent());
        projectShare.setShareMessage(request.getShareMessage());
        
        // Auto-accept for now (you can implement approval workflow later)
        projectShare.acceptShare(currentUser.getId());
        
        projectShareRepository.save(projectShare);
        logger.info("Successfully shared project {} with organization {} by {}, credits: initial={}, monthly={}", 
                   projectId, targetOrganizationId, currentUserOrgId, 
                   request.getInitialCredits(), request.getMonthlyRecurringCredits());
    }

    /**
     * Legacy method for backward compatibility
     */
    @PreAuthorize("hasPermission('project', 'share')")
    @Transactional
    public void shareProject(String projectId, String targetOrganizationId, User currentUser) {
        // Check if user has permission to share projects (only ORG_ADMIN, ORG_OWNER, SUPER_ADMIN)
        if (!currentUser.getRole().canShareProjects()) {
            throw new IllegalArgumentException("Only organization admins can share projects. Your role: " + 
                currentUser.getRole().getDisplayName());
        }

        ShareProjectRequest request = new ShareProjectRequest();
        request.setOrganizationId(targetOrganizationId);
        request.setInitialCredits(0.0);
        request.setMonthlyRecurringCredits(0.0);
        shareProject(projectId, request, currentUser);
    }

    /**
     * Resolve target organization ID from either organizationId or userEmail.
     * If userEmail is provided but user doesn't exist, create an invitation.
     */
    private String resolveTargetOrganization(ShareProjectRequest request, String projectId, User currentUser) {
        if (request.getOrganizationId() != null && !request.getOrganizationId().trim().isEmpty()) {
            return request.getOrganizationId();
        }
        
        if (request.getUserEmail() != null && !request.getUserEmail().trim().isEmpty()) {
            // Try to find user by email
            Optional<User> targetUserOpt = userRepository.findByEmail(request.getUserEmail());
            
            if (targetUserOpt.isPresent()) {
                // User exists - get their organization
                User targetUser = targetUserOpt.get();
                if (targetUser.getOrganizationId() == null) {
                    throw new IllegalArgumentException("User does not belong to any organization: " + request.getUserEmail());
                }
                return targetUser.getOrganizationId();
            } else {
                // User doesn't exist - create and send invitation
                createAndSendInvitation(projectId, request, currentUser);
                // Return null to indicate invitation was sent instead of direct sharing
                return null;
            }
        }
        
        throw new IllegalArgumentException("Either organizationId or userEmail must be provided");
    }

    /**
     * Create and send project invitation to non-existent user.
     */
    private void createAndSendInvitation(String projectId, ShareProjectRequest request, User currentUser) {
        logger.info("Creating invitation for non-existent user: {} for project: {}", 
                   request.getUserEmail(), projectId);

        // Check if there's already a pending invitation for this email and project
        boolean existingInvitation = projectInvitationRepository
            .existsByInviteEmailAndProjectIdAndStatus(request.getUserEmail(), projectId, InvitationStatus.PENDING);
        
        if (existingInvitation) {
            throw new IllegalArgumentException("An invitation for this project has already been sent to this email address");
        }

        // Create invitation
        ProjectInvitation invitation = new ProjectInvitation(
            projectId,
            currentUser.getId(),
            currentUser.getOrganizationId(),
            request.getUserEmail()
        );

        // Set share details
        invitation.setInitialCredits(request.getInitialCredits());
        invitation.setMonthlyRecurringCredits(request.getMonthlyRecurringCredits());
        invitation.setCanViewAnalytics(request.getCanViewAnalytics());
        invitation.setCanManageSessions(request.getCanManageSessions());
        invitation.setCanShareFurther(request.getCanShareFurther());
        invitation.setIsPermanent(request.getIsPermanent());
        invitation.setShareMessage(request.getShareMessage());

        // Save invitation
        invitation = projectInvitationRepository.save(invitation);

        // Send invitation email
        try {
            Project project = projectRepository.findById(projectId).orElse(null);
            Organization invitingOrg = organizationRepository.findById(currentUser.getOrganizationId()).orElse(null);
            
            if (project != null && invitingOrg != null) {
                invitationEmailService.sendProjectInvitationEmail(invitation, project, currentUser, invitingOrg);
                logger.info("Successfully sent invitation email to: {} for project: {}", 
                           request.getUserEmail(), projectId);
            } else {
                logger.error("Failed to send invitation email - missing project or organization data");
                throw new RuntimeException("Failed to send invitation email");
            }
        } catch (Exception e) {
            logger.error("Failed to send invitation email to: {}", request.getUserEmail(), e);
            // Don't throw exception here - invitation is created, email sending is best effort
        }
    }

    /**
     * Validate that the sharing organization has enough credits and deduct them
     */
    private void validateAndDeductCredits(String organizationId, Double creditsToDeduct) {
        if (creditsToDeduct == null || creditsToDeduct <= 0) {
            return; // No credits to deduct
        }
        
        // Find the organization
        Organization sharingOrg = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + organizationId));
        
        // Check if organization has enough credits
        Double availableCredits = sharingOrg.getRemainingCredits();
        if (availableCredits == null || availableCredits < creditsToDeduct) {
            throw new IllegalArgumentException("Insufficient credits. Available: " + 
                (availableCredits != null ? availableCredits : 0) + 
                ", Required: " + creditsToDeduct);
        }
        
        // Deduct credits by updating remaining credits
        sharingOrg.setRemainingCredits(availableCredits - creditsToDeduct);
        
        // Also update used credits
        Double currentUsed = sharingOrg.getUsedCredits();
        sharingOrg.setUsedCredits((currentUsed != null ? currentUsed : 0) + creditsToDeduct);
        
        organizationRepository.save(sharingOrg);
        
        logger.info("Deducted {} credits from organization {}. Remaining: {}", 
                   creditsToDeduct, organizationId, sharingOrg.getRemainingCredits());
    }

    /**
     * Unshare a project from an organization.
     */
    @PreAuthorize("hasPermission('project', 'share')")
    @Transactional
    public void unshareProject(String projectId, String targetOrganizationId, User currentUser) {
        logger.info("Unsharing project {} from organization: {}", projectId, targetOrganizationId);

        // Check if user has permission to share/unshare projects (only ORG_ADMIN, ORG_OWNER, SUPER_ADMIN)
        if (!currentUser.getRole().canShareProjects()) {
            throw new IllegalArgumentException("Only organization admins can unshare projects. Your role: " + 
                currentUser.getRole().getDisplayName());
        }

        // Find the project and ensure the user owns it
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        // Verify the user owns this project
        if (!project.getOrganizationId().equals(currentUser.getOrganizationId())) {
            throw new IllegalArgumentException("You can only unshare projects you own");
        }

        // Check if currently shared
        if (!project.isSharedWithOrganization(targetOrganizationId)) {
            throw new IllegalArgumentException("Project is not shared with this organization");
        }

        // Remove organization from shared list (simple implementation)
        String currentShared = project.getSharedWithOrganizations();
        String updatedShared = currentShared.replace("\"" + targetOrganizationId + "\"", "")
                                          .replace(",,", ",")
                                          .replace("[,", "[")
                                          .replace(",]", "]");
        if (updatedShared.equals("[]")) {
            project.setSharedWithOrganizations(null);
        } else {
            project.setSharedWithOrganizations(updatedShared);
        }

        projectRepository.save(project);
        logger.info("Successfully unshared project {} from organization {}", projectId, targetOrganizationId);
    }

    /**
     * Allocate credits to a project.
     */
    @PreAuthorize("hasPermission('project', 'manage_credits')")
    @Transactional
    public ProjectResponse allocateCredits(String projectId, Double credits, User currentUser) {
        logger.info("Allocating {} credits to project: {}", credits, projectId);

        Project project = findProjectWithAccess(projectId, currentUser);
        
        // Verify the user owns this project (only owners can allocate credits)
        if (!project.getOrganizationId().equals(currentUser.getOrganizationId())) {
            throw new IllegalArgumentException("You can only allocate credits to projects you own");
        }

        project.allocateCredits(credits);
        Project savedProject = projectRepository.save(project);

        ProjectResponse response = ProjectResponse.from(savedProject);
        
        // Set access type
        response.setAccessType("OWNED");
        response.setSharedBy(null);

        logger.info("Successfully allocated {} credits to project {}. New balance: {}", 
                   credits, projectId, savedProject.getRemainingCredits());

        return response;
    }

    /**
     * Helper method to get organization name by ID
     */
    private String getOrganizationName(String organizationId) {
        return organizationRepository.findById(organizationId)
                .map(org -> org.getName())
                .orElse("Unknown Organization");
    }

    /**
     * DTO for project statistics.
     */
    public static class ProjectStatistics {
        private final long totalProjects;
        private final long runningProjects;
        private final long stoppedProjects;
        private final long deployingProjects;
        private final long errorProjects;
        private final double totalCreditsUsed;
        private final long totalSessions;

        public ProjectStatistics(long totalProjects, long runningProjects, long stoppedProjects, 
                               long deployingProjects, long errorProjects, double totalCreditsUsed, long totalSessions) {
            this.totalProjects = totalProjects;
            this.runningProjects = runningProjects;
            this.stoppedProjects = stoppedProjects;
            this.deployingProjects = deployingProjects;
            this.errorProjects = errorProjects;
            this.totalCreditsUsed = totalCreditsUsed;
            this.totalSessions = totalSessions;
        }

        // Getters
        public long getTotalProjects() { return totalProjects; }
        public long getRunningProjects() { return runningProjects; }
        public long getStoppedProjects() { return stoppedProjects; }
        public long getDeployingProjects() { return deployingProjects; }
        public long getErrorProjects() { return errorProjects; }
        public double getTotalCreditsUsed() { return totalCreditsUsed; }
        public long getTotalSessions() { return totalSessions; }
    }

    /**
     * Get all projects shared by the current organization with usage statistics.
     */
    @PreAuthorize("hasPermission('project', 'read')")
    public List<SharedProjectUsageResponse> getSharedProjectUsage(User currentUser) {
        logger.info("Getting shared project usage for organization: {}", currentUser.getOrganizationId());

        String currentOrgId = currentUser.getOrganizationId();
        List<SharedProjectUsageResponse> usageList = new ArrayList<>();

        // Get all direct shares (projects originally owned by current organization)
        List<ProjectShare> directShares = projectShareRepository.findDirectSharesByOrganization(currentOrgId);
        for (ProjectShare share : directShares) {
            SharedProjectUsageResponse usage = buildSharedProjectUsage(share, "DIRECT");
            if (usage != null) {
                usageList.add(usage);
            }
        }

        // Get all indirect shares (projects the current organization reshared)
        List<ProjectShare> indirectShares = projectShareRepository.findIndirectSharesByOrganization(currentOrgId);
        for (ProjectShare share : indirectShares) {
            SharedProjectUsageResponse usage = buildSharedProjectUsage(share, "INDIRECT");
            if (usage != null) {
                usageList.add(usage);
            }
        }

        logger.info("Found {} shared projects for organization {}", usageList.size(), currentOrgId);
        return usageList;
    }

    /**
     * Build SharedProjectUsageResponse from ProjectShare entity.
     */
    private SharedProjectUsageResponse buildSharedProjectUsage(ProjectShare share, String shareType) {
        try {
            // Get project details
            Project project = projectRepository.findById(share.getProjectId()).orElse(null);
            if (project == null) {
                logger.warn("Project not found: {}", share.getProjectId());
                return null;
            }

            // Get shared with organization details
            Organization sharedWithOrg = organizationRepository.findById(share.getSharedWithOrganizationId()).orElse(null);
            if (sharedWithOrg == null) {
                logger.warn("Organization not found: {}", share.getSharedWithOrganizationId());
                return null;
            }

        // Get admin email (owner email from the shared with organization)
        String adminEmail = sharedWithOrg.getOwnerEmail();

        // Create response object
        SharedProjectUsageResponse usage = new SharedProjectUsageResponse(
            project.getId(),
            project.getName(),
            share.getSharedWithOrganizationId(),
            sharedWithOrg.getName(),
            adminEmail,
            shareType
        );

        // Set credit information
        usage.setCreditsAllocated(share.getAllocatedCredits());
        usage.setCreditsUsed(share.getUsedCredits());
        usage.setCreditsRemaining(share.getRemainingCredits());
        usage.setMonthlyRecurringCredits(share.getRecurringCredits());
        usage.setSharedAt(share.getSharedAt());
        usage.setStatus(share.getStatus().toString());

        // Get session statistics for this project and organization
        Long totalSessions = vmSessionRepository.countSessionsByProjectAndOrganization(
            project.getId(), share.getSharedWithOrganizationId());
        Double creditsUsedFromSessions = vmSessionRepository.getTotalCreditsUsedByProjectAndOrganization(
            project.getId(), share.getSharedWithOrganizationId());
        LocalDateTime lastUsed = vmSessionRepository.getLastUsageByProjectAndOrganization(
            project.getId(), share.getSharedWithOrganizationId());

        usage.setTotalSessions(totalSessions != null ? totalSessions : 0L);
        usage.setLastUsed(lastUsed);

        // Override credits used with actual session usage if available
        if (creditsUsedFromSessions != null && creditsUsedFromSessions > 0) {
            usage.setCreditsUsed(creditsUsedFromSessions);
        }

        return usage;
        } catch (Exception e) {
            logger.error("Error building shared project usage for share: {}", share.getId(), e);
            return null;
        }
    }
}
