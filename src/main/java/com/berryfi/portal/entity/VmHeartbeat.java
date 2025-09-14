package com.berryfi.portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing VM heartbeat data.
 * Tracks periodic check-ins from VMs to monitor their activity.
 */
@Entity
@Table(name = "vm_heartbeats", indexes = {
    @Index(name = "idx_heartbeat_session", columnList = "sessionId"),
    @Index(name = "idx_heartbeat_timestamp", columnList = "timestamp"),
    @Index(name = "idx_heartbeat_status", columnList = "status")
})
public class VmHeartbeat {
    
    @Id
    @Column(name = "id", length = 50)
    private String id;
    
    @NotBlank(message = "Session ID is required")
    @Column(name = "session_id", nullable = false)
    private String sessionId;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "status")
    private String status; // ACTIVE, IDLE, BUSY
    
    @Column(name = "cpu_usage")
    private Double cpuUsage;
    
    @Column(name = "memory_usage")
    private Double memoryUsage;
    
    @Column(name = "disk_usage")
    private Double diskUsage;
    
    @Column(name = "network_in")
    private Double networkIn;
    
    @Column(name = "network_out")
    private Double networkOut;
    
    @Column(name = "active_processes")
    private Integer activeProcesses;
    
    @Column(name = "user_sessions")
    private Integer userSessions;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData; // JSON string for additional metrics
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public VmHeartbeat() {
        this.id = generateHeartbeatId();
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public VmHeartbeat(String sessionId, String status) {
        this();
        this.sessionId = sessionId;
        this.status = status;
    }

    public VmHeartbeat(String sessionId, String status, Double cpuUsage, Double memoryUsage) {
        this(sessionId, status);
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
    }

    private String generateHeartbeatId() {
        return "hb_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // Helper methods
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public boolean isIdle() {
        return "IDLE".equalsIgnoreCase(this.status);
    }

    public boolean isHighCpuUsage() {
        return cpuUsage != null && cpuUsage > 80.0;
    }

    public boolean isHighMemoryUsage() {
        return memoryUsage != null && memoryUsage > 80.0;
    }

    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        if (cpuUsage != null) metrics.put("cpu", cpuUsage);
        if (memoryUsage != null) metrics.put("memory", memoryUsage);
        if (diskUsage != null) metrics.put("disk", diskUsage);
        if (networkIn != null) metrics.put("networkIn", networkIn);
        if (networkOut != null) metrics.put("networkOut", networkOut);
        if (activeProcesses != null) metrics.put("activeProcesses", activeProcesses);
        if (userSessions != null) metrics.put("userSessions", userSessions);
        return metrics;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public Double getNetworkIn() {
        return networkIn;
    }

    public void setNetworkIn(Double networkIn) {
        this.networkIn = networkIn;
    }

    public Double getNetworkOut() {
        return networkOut;
    }

    public void setNetworkOut(Double networkOut) {
        this.networkOut = networkOut;
    }

    public Integer getActiveProcesses() {
        return activeProcesses;
    }

    public void setActiveProcesses(Integer activeProcesses) {
        this.activeProcesses = activeProcesses;
    }

    public Integer getUserSessions() {
        return userSessions;
    }

    public void setUserSessions(Integer userSessions) {
        this.userSessions = userSessions;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
