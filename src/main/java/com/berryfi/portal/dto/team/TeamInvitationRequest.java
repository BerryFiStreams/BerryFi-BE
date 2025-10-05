package com.berryfi.portal.dto.team;

import com.berryfi.portal.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for team member invitations.
 */
public class TeamInvitationRequest {
    
    @NotBlank(message = "User email is required")
    @Email(message = "Valid email address is required")
    private String userEmail;
    
    @NotNull(message = "Role is required")
    private Role role;
    
    private String message;
    
    // Constructors
    public TeamInvitationRequest() {}
    
    public TeamInvitationRequest(String userEmail, Role role, String message) {
        this.userEmail = userEmail;
        this.role = role;
        this.message = message;
    }
    
    // Getters and Setters
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "TeamInvitationRequest{" +
                "userEmail='" + userEmail + '\'' +
                ", role=" + role +
                ", message='" + message + '\'' +
                '}';
    }
}