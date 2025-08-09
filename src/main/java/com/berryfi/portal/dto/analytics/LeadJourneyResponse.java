package com.berryfi.portal.dto.analytics;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for lead journey analytics.
 */
public class LeadJourneyResponse {
    private String leadId;
    private String firstName;
    private String lastName;
    private String email;
    private List<Map<String, Object>> touchpoints;
    private Map<String, Object> summary;
    private String currentStage;
    private double journeyProgress;

    public LeadJourneyResponse() {}

    public LeadJourneyResponse(String leadId, String firstName, String lastName, String email,
                              List<Map<String, Object>> touchpoints, Map<String, Object> summary,
                              String currentStage, double journeyProgress) {
        this.leadId = leadId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.touchpoints = touchpoints;
        this.summary = summary;
        this.currentStage = currentStage;
        this.journeyProgress = journeyProgress;
    }

    // Getters and setters
    public String getLeadId() { return leadId; }
    public void setLeadId(String leadId) { this.leadId = leadId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Map<String, Object>> getTouchpoints() { return touchpoints; }
    public void setTouchpoints(List<Map<String, Object>> touchpoints) { this.touchpoints = touchpoints; }

    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }

    public double getJourneyProgress() { return journeyProgress; }
    public void setJourneyProgress(double journeyProgress) { this.journeyProgress = journeyProgress; }
}
