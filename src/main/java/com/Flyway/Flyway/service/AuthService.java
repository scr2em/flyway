package com.Flyway.Flyway.service;

import com.Flyway.Flyway.dto.request.LoginRequest;
import com.Flyway.Flyway.dto.request.RefreshTokenRequest;
import com.Flyway.Flyway.dto.request.UserRegistrationRequest;
import com.Flyway.Flyway.dto.response.AuthResponse;
import com.Flyway.Flyway.dto.response.UserResponse;
import com.Flyway.Flyway.exception.ConflictException;
import com.Flyway.Flyway.exception.UnauthorizedException;
import com.Flyway.Flyway.repository.RefreshTokenRepository;
import com.Flyway.Flyway.repository.UserRepository;
import com.Flyway.Flyway.repository.UserStatusRepository;
import com.Flyway.Flyway.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    
    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        // Check if user exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }
        
        // Get active status
        Record activeStatus = userStatusRepository.findByCode("active")
                .orElseThrow(() -> new RuntimeException("Active status not found"));
        
        // Create user
        String userId = userRepository.create(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                activeStatus.get("id", String.class)
        );
        
        // Generate tokens
        String accessToken = jwtUtil.generateToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        
        // Store refresh token
        storeRefreshToken(userId, refreshToken, null);
        
        // Get user details
        UserResponse user = userService.getUserById(userId);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(user)
                .build();
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // Find user by email
        Record userRecord = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        
        String userId = userRecord.get("id", String.class);
        String passwordHash = userRecord.get("password_hash", String.class);
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), passwordHash)) {
            throw new UnauthorizedException("Invalid credentials");
        }
        
        // Update last login
        userRepository.updateLastLogin(userId);
        
        // Generate tokens
        String accessToken = jwtUtil.generateToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        
        // Store refresh token
        String deviceInfo = httpRequest.getHeader("User-Agent");
        String ipAddress = httpRequest.getRemoteAddr();
        storeRefreshToken(userId, refreshToken, deviceInfo, ipAddress);
        
        // Get user details
        UserResponse user = userService.getUserById(userId);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(user)
                .build();
    }
    
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // Validate token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        
        // Extract user ID
        String userId = jwtUtil.extractUserId(refreshToken);
        
        // Hash and verify token exists in database
        String tokenHash = hashToken(refreshToken);
        Record tokenRecord = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));
        
        // Check if revoked
        if (tokenRecord.get("is_revoked", Boolean.class)) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        
        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(userId);
        
        // Get user details
        UserResponse user = userService.getUserById(userId);
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(user)
                .build();
    }
    
    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = hashToken(refreshToken);
        refreshTokenRepository.revokeByTokenHash(tokenHash);
    }
    
    @Transactional
    public void logoutAll(String userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
    
    private void storeRefreshToken(String userId, String refreshToken, String deviceInfo) {
        storeRefreshToken(userId, refreshToken, deviceInfo, null);
    }
    
    private void storeRefreshToken(String userId, String refreshToken, String deviceInfo, String ipAddress) {
        String tokenHash = hashToken(refreshToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7); // 7 days
        refreshTokenRepository.create(userId, tokenHash, expiresAt, deviceInfo, ipAddress);
    }
    
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
}

