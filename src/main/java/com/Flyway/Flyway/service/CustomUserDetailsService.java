package com.Flyway.Flyway.service;

import com.Flyway.Flyway.repository.OrganizationMemberRepository;
import com.Flyway.Flyway.repository.UserRepository;
import com.Flyway.Flyway.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Record user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        
        // Get user's organization ID
        String organizationId = null;
        List<Record> memberships = organizationMemberRepository.findByUserId(userId);
        if (!memberships.isEmpty()) {
            organizationId = memberships.get(0).get("organization_id", String.class);
        }
        
        return CustomUserDetails.builder()
                .id(user.get("id", String.class))
                .email(user.get("email", String.class))
                .password(user.get("password_hash", String.class))
                .emailVerified(user.get("email_verified", Boolean.class))
                .userStatusCode(getUserStatusCode(user.get("user_status_id", String.class)))
                .organizationId(organizationId)
                .build();
    }
    
    private String getUserStatusCode(String userStatusId) {
        // This would ideally join with user_statuses table
        // For now, we'll fetch it separately or use a join in the repository
        return "active"; // Placeholder
    }
}

