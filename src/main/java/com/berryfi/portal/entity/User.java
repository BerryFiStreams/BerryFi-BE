package com.berryfi.portal.entity;

import com.berryfi.portal.enums.AccountType;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a user in the system.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_organization", columnList = "organizationId"),
    @Index(name = "idx_user_workspace", columnList = "workspaceId")
})
public class User implements UserDetails {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "workspace_id")
    private String workspaceId;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @Column(name = "refresh_token")
    private String refreshToken;

    public User() {
        this.id = generateUserId();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String name, String email, String password, Role role, AccountType accountType) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.accountType = accountType;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateUserId() {
        return "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    // UserDetails implementation
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + role.getValue().toUpperCase());
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return status != UserStatus.DISABLED;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Helper methods for role checking
    public boolean hasAdminPrivileges() {
        return role.hasAdminPrivileges();
    }

    public boolean canAccessBilling() {
        return role.canAccessBilling();
    }

    public boolean canAccessAudit() {
        return role.canAccessAudit();
    }

    public boolean canAccessReports() {
        return role.canAccessReports();
    }

    public boolean isOrganizationLevel() {
        return role.isOrganizationLevel();
    }

    public boolean isWorkspaceLevel() {
        return role.isWorkspaceLevel();
    }
}
