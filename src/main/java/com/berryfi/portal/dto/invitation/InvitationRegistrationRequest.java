package com.berryfi.portal.dto.invitation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration through project invitation.
 */
public class InvitationRegistrationRequest {

    @NotBlank(message = "Invitation token is required")
    private String inviteToken;

    // User details
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String fullName;

    @Email(message = "Valid email address is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    // Organization details
    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String organizationName;

    @Size(max = 500, message = "Organization description cannot exceed 500 characters")
    private String organizationDescription;

    // Optional fields
    private String phoneNumber;
    private String jobTitle;
    private String company;

    // Constructors
    public InvitationRegistrationRequest() {}

    public InvitationRegistrationRequest(String inviteToken, String fullName, String email, 
                                       String password, String organizationName) {
        this.inviteToken = inviteToken;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.organizationName = organizationName;
    }

    // Getters and Setters
    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationDescription() {
        return organizationDescription;
    }

    public void setOrganizationDescription(String organizationDescription) {
        this.organizationDescription = organizationDescription;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "InvitationRegistrationRequest{" +
                "inviteToken='" + inviteToken + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", organizationName='" + organizationName + '\'' +
                '}';
    }
}