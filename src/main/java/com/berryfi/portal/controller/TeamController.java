package com.berryfi.portal.controller;

import com.berryfi.portal.dto.team.TeamStatsResponse;
import com.berryfi.portal.service.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for team-level operations.
 */
@RestController
@RequestMapping("/api/team")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TeamController {

    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);

    @Autowired
    private TeamService teamService;

    /**
     * Get team statistics.
     * GET /api/team/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<TeamStatsResponse> getTeamStats(
            @RequestParam(required = false) String workspaceId,
            @RequestHeader("X-Organization-ID") String organizationId) {
        logger.info("Getting team stats for organization: {}, workspace: {}", organizationId, workspaceId);

        try {
            TeamStatsResponse stats = teamService.getTeamStats(organizationId, workspaceId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            logger.error("Error getting team stats: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
