package com.berryfi.portal.controller;

import com.berryfi.portal.dto.auth.AuthResponse;
import com.berryfi.portal.dto.auth.ForgotPasswordRequest;
import com.berryfi.portal.dto.auth.LoginRequest;
import com.berryfi.portal.dto.auth.RefreshTokenRequest;
import com.berryfi.portal.dto.auth.RefreshTokenResponse;
import com.berryfi.portal.dto.auth.RegisterRequest;
import com.berryfi.portal.dto.auth.ResetPasswordRequest;
import com.berryfi.portal.dto.error.ApiError;
import com.berryfi.portal.dto.user.UserDto;
import com.berryfi.portal.exception.AuthenticationException;
import com.berryfi.portal.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * User login endpoint.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            ApiError error = new ApiError(401, e.getMessage(), "Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            ApiError error = new ApiError(400, "Bad Request", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * User registration endpoint.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse response = authService.register(registerRequest);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            ApiError error = new ApiError(400, e.getMessage(), "Bad Request");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            ApiError error = new ApiError(400, "Bad Request", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Refresh access token endpoint.
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshTokenResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            ApiError error = new ApiError(401, e.getMessage(), "Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            ApiError error = new ApiError(400, "Bad Request", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get current user endpoint.
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            UserDto user = authService.getCurrentUser();
            return ResponseEntity.ok(user);
        } catch (AuthenticationException e) {
            ApiError error = new ApiError(401, e.getMessage(), "Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            ApiError error = new ApiError(403, "Forbidden", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    /**
     * User logout endpoint.
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            authService.logout();
            return ResponseEntity.noContent().build();
        } catch (AuthenticationException e) {
            ApiError error = new ApiError(401, e.getMessage(), "Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            ApiError error = new ApiError(500, "Internal Server Error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Forgot password endpoint - initiates password reset flow.
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
            // Always return success to prevent email enumeration attacks
            return ResponseEntity.ok().body(java.util.Map.of(
                "message", "If an account exists with this email, a password reset link has been sent."
            ));
        } catch (Exception e) {
            // Log error but still return success to prevent email enumeration
            System.err.println("Error during forgot password: " + e.getMessage());
            return ResponseEntity.ok().body(java.util.Map.of(
                "message", "If an account exists with this email, a password reset link has been sent."
            ));
        }
    }

    /**
     * Reset password endpoint - completes password reset flow.
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok().body(java.util.Map.of(
                "message", "Password has been reset successfully. You can now log in with your new password."
            ));
        } catch (AuthenticationException e) {
            ApiError error = new ApiError(400, e.getMessage(), "Bad Request");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            ApiError error = new ApiError(500, "Internal Server Error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
