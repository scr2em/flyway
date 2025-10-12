package com.Flyway.Flyway.service;

import com.Flyway.Flyway.dto.request.UpdateUserRequest;
import com.Flyway.Flyway.dto.response.UserResponse;
import com.Flyway.Flyway.exception.ResourceNotFoundException;
import com.Flyway.Flyway.repository.UserRepository;
import com.Flyway.Flyway.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    
    public UserResponse getUserById(String id) {
        Record user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        return mapToUserResponse(user);
    }
    
    public UserResponse getUserByEmail(String email) {
        Record user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        return mapToUserResponse(user);
    }
    
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        // Check if user exists
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // Update user
        userRepository.update(id, request.getFirstName(), request.getLastName());
        
        // Return updated user
        return getUserById(id);
    }
    
    @Transactional
    public void deleteUser(String id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        userRepository.delete(id);
    }
    
    @Transactional
    public UserResponse verifyEmail(String id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        userRepository.verifyEmail(id);
        return getUserById(id);
    }
    
    private UserResponse mapToUserResponse(Record record) {
        String userStatusId = record.get("user_status_id", String.class);
        Record statusRecord = userStatusRepository.findById(userStatusId).orElse(null);
        
        return UserResponse.builder()
                .id(record.get("id", String.class))
                .firstName(record.get("first_name", String.class))
                .lastName(record.get("last_name", String.class))
                .email(record.get("email", String.class))
                .userStatusId(userStatusId)
                .userStatusCode(statusRecord != null ? statusRecord.get("code", String.class) : null)
                .userStatusLabel(statusRecord != null ? statusRecord.get("label", String.class) : null)
                .emailVerified(record.get("email_verified", Boolean.class))
                .emailVerifiedAt(record.get("email_verified_at", LocalDateTime.class))
                .lastLoginAt(record.get("last_login_at", LocalDateTime.class))
                .createdAt(record.get("created_at", LocalDateTime.class))
                .updatedAt(record.get("updated_at", LocalDateTime.class))
                .build();
    }
}

