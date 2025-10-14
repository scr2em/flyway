package com.Flyway.server.service;

import com.Flyway.server.dto.generated.UpdateUserRequest;
import com.Flyway.server.dto.generated.UserResponse;
import com.Flyway.server.dto.generated.UserResponseOrganization;
import com.Flyway.server.dto.generated.UserStatusResponse;
import com.Flyway.server.dto.generated.UserStatusEnum;
import com.Flyway.server.dto.generated.InvitationStatusResponse;
import com.Flyway.server.dto.generated.InvitationStatusEnum;
import com.Flyway.server.jooq.tables.records.OrganizationMembersRecord;
import com.Flyway.server.jooq.tables.records.OrganizationsRecord;
import com.Flyway.server.jooq.tables.records.UsersRecord;
import com.Flyway.server.jooq.tables.records.InvitationsRecord;
import com.Flyway.server.jooq.tables.records.InvitationStatusesRecord;
import com.Flyway.server.exception.ForbiddenException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.repository.OrganizationMemberRepository;
import com.Flyway.server.repository.OrganizationRepository;
import com.Flyway.server.repository.UserRepository;
import com.Flyway.server.repository.UserStatusRepository;
import com.Flyway.server.repository.InvitationRepository;
import com.Flyway.server.repository.InvitationStatusRepository;
import com.Flyway.server.jooq.tables.records.UserStatusesRecord;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final PermissionService permissionService;
    private final InvitationRepository invitationRepository;
    private final InvitationStatusRepository invitationStatusRepository;
    
    public UserResponse getUserById(String id) {
        UsersRecord user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        return mapToUserResponse(user);
    }
    
    public UserResponse getUserByEmail(String email) {
        UsersRecord user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        return mapToUserResponse(user);
    }
    
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    public List<UserResponse> getUsersByOrganizationId(String organizationId) {
        // Get all members of the organization
        List<OrganizationMembersRecord> members = organizationMemberRepository.findByOrganizationId(organizationId);
        
        // Map to user responses
        return members.stream()
                .map(member -> {
                    UsersRecord user = userRepository.findById(member.getUserId()).orElse(null);
                    return user != null ? mapToUserResponse(user) : null;
                })
                .filter(userResponse -> userResponse != null)
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
        
        // Check if user is an organization owner
        List<?> ownedOrganizations = organizationRepository.findByCreatedBy(id);
        if (!ownedOrganizations.isEmpty()) {
            throw new ForbiddenException("Cannot delete user who is an organization owner. Please transfer ownership or delete the organization first.");
        }
        
        userRepository.delete(id);
    }
    
    @Transactional
    public UserResponse verifyEmail(String id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        userRepository.verifyEmail(id);
        return getUserById(id);
    }
    
    public void checkUserPermission(String userId, String organizationId, String permissionCode) {
        if (organizationId == null) {
            throw new ForbiddenException("User is not a member of any organization");
        }
        
        boolean hasPermission = permissionService.userHasPermission(userId, organizationId, permissionCode);
        if (!hasPermission) {
            throw new ForbiddenException(
                "You do not have permission to perform this action. Required permission: " + permissionCode
            );
        }
    }
    
    public void verifyUserInOrganization(String userId, String organizationId) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Get user's organization membership
        List<OrganizationMembersRecord> memberships = organizationMemberRepository.findByUserId(userId);
        
        if (memberships.isEmpty()) {
            throw new ForbiddenException("User is not a member of any organization");
        }
        
        // Check if user belongs to the specified organization
        boolean isInOrganization = memberships.stream()
                .anyMatch(membership -> membership.getOrganizationId().equals(organizationId));
        
        if (!isInOrganization) {
            throw new ForbiddenException("You do not have access to this user");
        }
    }
    
    private UserResponse mapToUserResponse(UsersRecord record) {
        String userId = record.getId();
        String userStatusId = record.getUserStatusId();
        UserStatusesRecord statusRecord = userStatusRepository.findById(userStatusId).orElse(null);
        
        // Create UserStatusResponse
        UserStatusResponse statusResponse = null;
        if (statusRecord != null) {
            String statusCode = statusRecord.getCode();
            UserStatusEnum statusEnum = UserStatusEnum.fromValue(statusCode);
            statusResponse = new UserStatusResponse()
                    .id(userStatusId)
                    .status(statusEnum);
        }
        
        // Get organization from organization_members table (user can only be in 1 organization)
        UserResponseOrganization organization = null;
        List<OrganizationMembersRecord> orgMembers = organizationMemberRepository.findByUserId(userId);
        if (!orgMembers.isEmpty()) {
            String organizationId = orgMembers.get(0).getOrganizationId();
            OrganizationsRecord orgRecord = organizationRepository.findById(organizationId).orElse(null);
            
            if (orgRecord != null) {
                organization = new UserResponseOrganization()
                        .id(orgRecord.getId())
                        .name(orgRecord.getName());
            }
        }
        
        // Get invitation status by joining with invitations table
        // For organization owners who were not invited, this will be null
        InvitationStatusResponse invitationStatusResponse = null;
        List<InvitationsRecord> invitations = invitationRepository.findByEmail(record.getEmail());
        if (!invitations.isEmpty()) {
            // Get the most recent invitation
            InvitationsRecord invitation = invitations.get(0);
            String invitationStatusId = invitation.getInvitationStatusId();
            InvitationStatusesRecord invitationStatusRecord = invitationStatusRepository.findById(invitationStatusId).orElse(null);
            
            if (invitationStatusRecord != null) {
                String invitationStatusCode = invitationStatusRecord.getCode();
                InvitationStatusEnum invitationStatusEnum = InvitationStatusEnum.fromValue(invitationStatusCode);
                invitationStatusResponse = new InvitationStatusResponse()
                        .id(invitationStatusId)
                        .status(invitationStatusEnum);
            }
        }
        
        // Convert LocalDateTime to OffsetDateTime
        LocalDateTime createdAtLocal = record.getCreatedAt();
        LocalDateTime updatedAtLocal = record.getUpdatedAt();
        
        return new UserResponse()
                .id(userId)
                .firstName(record.getFirstName())
                .lastName(record.getLastName())
                .email(record.getEmail())
                .status(statusResponse)
                .organization(organization)
                .invitationStatus(invitationStatusResponse)
                .createdAt(createdAtLocal != null ? createdAtLocal.atOffset(ZoneOffset.UTC) : null)
                .updatedAt(updatedAtLocal != null ? updatedAtLocal.atOffset(ZoneOffset.UTC) : null);
    }
}

