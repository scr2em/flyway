package com.Flyway.server.service;

import com.Flyway.server.jooq.tables.records.OrganizationMembersRecord;
import com.Flyway.server.jooq.tables.records.UsersRecord;
import com.Flyway.server.repository.OrganizationMemberRepository;
import com.Flyway.server.repository.UserRepository;
import com.Flyway.server.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
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
        UsersRecord user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        
        // Get user's organization ID
        String organizationId = null;
        List<OrganizationMembersRecord> memberships = organizationMemberRepository.findByUserId(userId);
        if (!memberships.isEmpty()) {
            organizationId = memberships.get(0).getOrganizationId();
        }
        
        // email_verified is stored as byte (0 = false, 1 = true)
        Boolean emailVerified = user.getEmailVerified() != 0;
        
        return CustomUserDetails.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .emailVerified(emailVerified)
                .userStatusCode(getUserStatusCode(user.getUserStatusId()))
                .organizationId(organizationId)
                .build();
    }
    
    private String getUserStatusCode(String userStatusId) {
        // This would ideally join with user_statuses table
        // For now, we'll fetch it separately or use a join in the repository
        return "active"; // Placeholder
    }
}

