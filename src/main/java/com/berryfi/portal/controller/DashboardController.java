package com.berryfi.portal.controller;

import com.berryfi.portal.dto.dashboard.DashboardResponse;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Dashboard statistics for the authenticated user's organization")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    /**
     * GET /api/dashboard
     * Returns org-scoped summary stats and recent projects.
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal User currentUser) {
        logger.info("Dashboard request for organization: {}", currentUser.getOrganizationId());
        DashboardResponse response = dashboardService.getDashboardData(currentUser);
        return ResponseEntity.ok(response);
    }
}
