package com.berryfi.portal.controller;

import com.berryfi.portal.service.UrlTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Controller for URL tracking and redirection.
 */
@RestController
@RequestMapping("/track")
public class TrackingController {

    @Autowired
    private UrlTrackingService urlTrackingService;

    /**
     * Track URL access and redirect to original URL.
     * GET /track/{trackingData}
     */
    @GetMapping("/{trackingData}")
    public void trackAndRedirect(@PathVariable String trackingData,
                               @RequestHeader(value = "Referer", required = false) String referrerUrl,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        
        // Track the URL access
        urlTrackingService.trackUrlAccess(trackingData, referrerUrl, request);
        
        // For now, redirect to a default page or return a success message
        // In production, you would decode the tracking data and redirect to the actual project URL
        response.sendRedirect("https://berryfi.app");
    }

    /**
     * Generate tracking URL for a project.
     * POST /track/generate
     */
    @PostMapping("/generate")
    public String generateTrackingUrl(@RequestParam String projectId,
                                    @RequestParam String userId,
                                    @RequestParam(defaultValue = "https://berryfi.app") String baseUrl) {
        return urlTrackingService.generateTrackingUrl(projectId, userId, baseUrl);
    }
}
