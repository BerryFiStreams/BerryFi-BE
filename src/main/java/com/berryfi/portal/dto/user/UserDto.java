package com.berryfi.portal.dto.user;

import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.AccountType;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * DTO for User entity.
 */
public class UserDto {

    private String id;
    private String name;
    private String email;
    private Role role;
    private AccountType accountType;
    private String organizationId;

    private UserStatus status;
    private LocalDateTime lastLogin;

    public UserDto() {}

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.accountType = user.getAccountType();
        this.organizationId = user.getOrganizationId();
        this.status = user.getStatus();
        this.lastLogin = user.getLastLogin();
    }

    public static UserDto fromUser(User user) {
        return new UserDto(user);
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
}
