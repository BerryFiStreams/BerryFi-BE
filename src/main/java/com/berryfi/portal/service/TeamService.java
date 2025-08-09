package com.berryfi.portal.service;

import com.berryfi.portal.dto.team.TeamStatsResponse;
import com.berryfi.portal.enums.CampaignStatus;
import com.berryfi.portal.enums.UserStatus;
import com.berryfi.portal.enums.LeadStatus;
import com.berryfi.portal.repository.CampaignRepository;
import com.berryfi.portal.repository.LeadRepository;
import com.berryfi.portal.repository.TeamMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for team-level operations.
 */
@Service
public class TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private LeadRepository leadRepository;

    /**
     * Get team statistics.
     */
    public TeamStatsResponse getTeamStats(String organizationId, String workspaceId) {
        logger.debug("Getting team stats for organization: {}, workspace: {}", organizationId, workspaceId);

        TeamStatsResponse response = new TeamStatsResponse();
        TeamStatsResponse.TeamStatsData data = response.getData();

        try {
            // Organization-wide stats (workspace filtering not implemented yet)
            data.setTotalMembers(teamMemberRepository.countByOrganizationId(organizationId).intValue());
            data.setActiveMembers(teamMemberRepository.countByOrganizationIdAndStatus(organizationId, UserStatus.ACTIVE).intValue());
            
            // Campaign stats for organization
            data.setTotalCampaigns(campaignRepository.countByOrganizationId(organizationId).intValue());
            data.setActiveCampaigns(campaignRepository.countByOrganizationIdAndStatus(organizationId, CampaignStatus.ACTIVE).intValue());
            
            // Lead stats for organization
            data.setTotalLeads(leadRepository.getTotalLeadsCount(organizationId).intValue());
            data.setQualifiedLeads(leadRepository.countByOrganizationIdAndStatus(organizationId, LeadStatus.QUALIFIED).intValue());
            data.setConvertedLeads(leadRepository.getConvertedLeadsCount(organizationId).intValue());

            // Calculate derived metrics
            if (data.getTotalLeads() > 0) {
                data.setConversionRate((double) data.getConvertedLeads() / data.getTotalLeads() * 100);
            }

            if (data.getTotalCampaigns() > 0) {
                data.setAvgLeadsPerCampaign((double) data.getTotalLeads() / data.getTotalCampaigns());
            }

            // Mock average conversion time (in hours)
            data.setAvgConversionTime(24.5);

            logger.debug("Team stats retrieved successfully for organization: {}", organizationId);

        } catch (Exception e) {
            logger.error("Error retrieving team stats: {}", e.getMessage());
            response.setSuccess(false);
        }

        return response;
    }
}
