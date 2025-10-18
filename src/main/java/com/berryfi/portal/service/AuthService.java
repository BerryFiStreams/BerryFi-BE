package com.berryfi.portal.service;

import com.berryfi.portal.dto.auth.AuthResponse;
import com.berryfi.portal.dto.auth.LoginRequest;
import com.berryfi.portal.dto.auth.RefreshTokenRequest;
import com.berryfi.portal.dto.auth.RefreshTokenResponse;
import com.berryfi.portal.dto.auth.RegisterRequest;
import com.berryfi.portal.dto.user.UserDto;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.UserStatus;
import com.berryfi.portal.exception.AuthenticationException;
import com.berryfi.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for authentication operations.
 */
@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InvitationEmailService invitationEmailService;

    /**
     * Authenticate user and return tokens.
     */
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();

            // Check if user is active
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new AuthenticationException("Account is not active");
            }

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Save refresh token and update last login
            user.setRefreshToken(refreshToken);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Return response
            UserDto userDto = UserDto.fromUser(user);
            return new AuthResponse(userDto, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid email or password");
        } catch (DisabledException e) {
            throw new AuthenticationException("Account is disabled");
        }
    }

    /**
     * Register a new user.
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AuthenticationException("User with this email already exists");
        }

        // Create new user
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());
        user.setAccountType(registerRequest.getAccountType());
        user.setStatus(UserStatus.ACTIVE);

        // Auto-generate organizationId if not provided
        if (registerRequest.getOrganizationId() == null || registerRequest.getOrganizationId().trim().isEmpty()) {
            user.setOrganizationId(generateOrganizationId());
        } else {
            user.setOrganizationId(registerRequest.getOrganizationId());
        }

        // Save user
        User savedUser = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        // Save refresh token
        savedUser.setRefreshToken(refreshToken);
        savedUser.setLastLogin(LocalDateTime.now());
        userRepository.save(savedUser);

        // Send welcome email
        try {
            invitationEmailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName(), "BerryFi Studio");
        } catch (Exception e) {
            // Log error but don't fail registration if welcome email fails
            // This will be handled gracefully in the sendWelcomeEmail method
        }

        // Return response
        UserDto userDto = UserDto.fromUser(savedUser);
        return new AuthResponse(userDto, accessToken, refreshToken);
    }

    /**
     * Refresh access token using refresh token.
     */
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        // Find user by refresh token
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        // Check if user is still active
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationException("Account is not active");
        }

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        return new RefreshTokenResponse(newAccessToken);
    }

    /**
     * Get current authenticated user.
     */
    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("No authenticated user found");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        return UserDto.fromUser(user);
    }

    /**
     * Logout user by clearing refresh token.
     */
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            userRepository.findByEmail(email).ifPresent(user -> {
                user.setRefreshToken(null);
                userRepository.save(user);
            });
        }

        SecurityContextHolder.clearContext();
    }

    /**
     * Get user by email.
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found with email: " + email));
    }

    /**
     * Check if user has specific role.
     */
    public boolean hasRole(User user, String roleName) {
        return user.getRole().getValue().equals(roleName);
    }

    /**
     * Check if user can access organization.
     */
    public boolean canAccessOrganization(User user, String organizationId) {
        if (user.getRole() == com.berryfi.portal.enums.Role.SUPER_ADMIN) {
            return true; // Super admin can access any organization
        } else {
            // Users can access their own organization
            return organizationId.equals(user.getOrganizationId());
        }
    }

    /**
     * Generate a unique organization ID.
     */
    private String generateOrganizationId() {
        return "org_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
