package com.berryfi.portal.dto.audit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for user activity summary.
 */
public class UserActivityResponse {
    private String userId;
    private String userName;
    private String email;
    private long totalActions;
    private LocalDateTime lastActivity;
    private LocalDateTime firstActivity;
    private Map<String, Long> actionsByType;
    private List<Map<String, Object>> recentActivity;
    private Map<String, Object> summary;

    public UserActivityResponse() {}

    public UserActivityResponse(String userId, String userName, String email, long totalActions,
                               LocalDateTime lastActivity, LocalDateTime firstActivity,
                               Map<String, Long> actionsByType, List<Map<String, Object>> recentActivity,
                               Map<String, Object> summary) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.totalActions = totalActions;
        this.lastActivity = lastActivity;
        this.firstActivity = firstActivity;
        this.actionsByType = actionsByType;
        this.recentActivity = recentActivity;
        this.summary = summary;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getTotalActions() { return totalActions; }
    public void setTotalActions(long totalActions) { this.totalActions = totalActions; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    public LocalDateTime getFirstActivity() { return firstActivity; }
    public void setFirstActivity(LocalDateTime firstActivity) { this.firstActivity = firstActivity; }

    public Map<String, Long> getActionsByType() { return actionsByType; }
    public void setActionsByType(Map<String, Long> actionsByType) { this.actionsByType = actionsByType; }

    public List<Map<String, Object>> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<Map<String, Object>> recentActivity) { this.recentActivity = recentActivity; }

    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }
}
