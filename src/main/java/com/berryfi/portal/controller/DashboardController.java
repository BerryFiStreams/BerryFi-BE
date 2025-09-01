package com.berryfi.portal.controller;

import com.berryfi.portal.dto.dashboard.DashboardResponse;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for dashboard operations.
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get dashboard data for the current user.
     * GET /api/dashboard
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal User currentUser) {

        logger.info("Getting dashboard data for user: {} in organization: {}", 
                   currentUser.getId(), currentUser.getOrganizationId());

        try {
            DashboardResponse dashboardData = dashboardService.getDashboardData(currentUser);
            return ResponseEntity.ok(new ApiResponse<>(true, dashboardData));
        } catch (Exception e) {
            logger.error("Error getting dashboard data for user: {}", currentUser.getId(), e);
            return ResponseEntity.ok(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    /**
     * Generic API response wrapper.
     */
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;

        public ApiResponse(boolean success, T data) {
            this.success = success;
            this.data = data;
        }

        public ApiResponse(boolean success, T data, String message) {
            this.success = success;
            this.data = data;
            this.message = message;
        }

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
