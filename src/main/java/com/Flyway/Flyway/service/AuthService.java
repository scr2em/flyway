package com.Flyway.Flyway.service;

import com.Flyway.Flyway.dto.generated.LoginRequest;
import com.Flyway.Flyway.dto.generated.RefreshTokenRequest;
import com.Flyway.Flyway.dto.generated.UserRegistrationRequest;
import com.Flyway.Flyway.dto.generated.AuthResponse;
import com.Flyway.Flyway.dto.generated.UserResponse;
import com.Flyway.Flyway.exception.ConflictException;
import com.Flyway.Flyway.exception.UnauthorizedException;
import com.Flyway.Flyway.jooq.tables.records.RefreshTokensRecord;
import com.Flyway.Flyway.jooq.tables.records.UsersRecord;
import com.Flyway.Flyway.jooq.tables.records.UserStatusesRecord;
import com.Flyway.Flyway.repository.RefreshTokenRepository;
import com.Flyway.Flyway.repository.UserRepository;
import com.Flyway.Flyway.repository.UserStatusRepository;
import com.Flyway.Flyway.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
        UserStatusesRecord activeStatus = userStatusRepository.findByCode("active")
                .orElseThrow(() -> new RuntimeException("Active status not found"));
        
        // Create user
        String userId = userRepository.create(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                activeStatus.getId()
        );
        
        // Generate tokens
        String accessToken = jwtUtil.generateToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        
        // Store refresh token
        storeRefreshToken(userId, refreshToken, null);
        
        // Get user details
        UserResponse user = userService.getUserById(userId);
        
        return new AuthResponse()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user);
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // Find user by email
        UsersRecord userRecord = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        
        String userId = userRecord.getId();
        String passwordHash = userRecord.getPasswordHash();
        
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
        
        return new AuthResponse()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user);
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
        RefreshTokensRecord tokenRecord = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));
        
        // Check if revoked (byte 1 = true)
        if (tokenRecord.getIsRevoked() != 0) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        
        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(userId);
        
        // Get user details
        UserResponse user = userService.getUserById(userId);
        
        return new AuthResponse()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .user(user);
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

