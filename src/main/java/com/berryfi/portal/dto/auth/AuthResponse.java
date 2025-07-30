package com.berryfi.portal.dto.auth;

import com.berryfi.portal.dto.user.UserDto;

/**
 * DTO for authentication response.
 */
public class AuthResponse {

    private UserDto user;
    private String accessToken;
    private String refreshToken;

    public AuthResponse() {}

    public AuthResponse(UserDto user, String accessToken, String refreshToken) {
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
