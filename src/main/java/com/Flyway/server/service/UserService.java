package com.Flyway.server.service;

import com.Flyway.server.dto.generated.UpdateUserRequest;
import com.Flyway.server.dto.generated.UserResponse;
import com.Flyway.server.dto.generated.UserOrganizationResponse;
import com.Flyway.server.dto.generated.UserOrganizationMembership;
import com.Flyway.server.dto.generated.RoleResponse;
import com.Flyway.server.dto.generated.UserStatusResponse;
import com.Flyway.server.dto.generated.UserStatusEnum;
import com.Flyway.server.dto.generated.InvitationStatusResponse;
import com.Flyway.server.dto.generated.InvitationStatusEnum;
import com.Flyway.server.jooq.tables.records.OrganizationMembersRecord;
import com.Flyway.server.jooq.tables.records.OrganizationsRecord;
import com.Flyway.server.jooq.tables.records.UsersRecord;
import com.Flyway.server.jooq.tables.records.InvitationsRecord;
import com.Flyway.server.jooq.tables.records.InvitationStatusesRecord;
import com.Flyway.server.jooq.tables.records.RolesRecord;
import com.Flyway.server.exception.ForbiddenException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.repository.OrganizationMemberRepository;
import com.Flyway.server.repository.OrganizationRepository;
import com.Flyway.server.repository.UserRepository;
import com.Flyway.server.repository.UserStatusRepository;
import com.Flyway.server.repository.InvitationRepository;
import com.Flyway.server.repository.InvitationStatusRepository;
import com.Flyway.server.repository.RoleRepository;
import com.Flyway.server.util.PermissionUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
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
    private final RoleRepository roleRepository;
    
    public UserResponse getUserById(String id) {
        UsersRecord user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        return mapToUserResponse(user);
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
    
  
    private UserResponse mapToUserResponse(UsersRecord record) {
        String userId = record.getId();
        
        // Fetch user status
        UserStatusResponse statusResponse = buildUserStatusResponse(record.getUserStatusId());
        
        // Fetch organization memberships with batch loading
        List<UserOrganizationMembership> organizations = buildOrganizationMemberships(userId, record.getEmail());
        
        return new UserResponse()
                .id(userId)
                .firstName(record.getFirstName())
                .lastName(record.getLastName())
                .email(record.getEmail())
                .status(statusResponse)
                .organizations(organizations)
                .createdAt(toOffsetDateTime(record.getCreatedAt()))
                .updatedAt(toOffsetDateTime(record.getUpdatedAt()));
    }
    
    private UserStatusResponse buildUserStatusResponse(String userStatusId) {
        if (userStatusId == null) {
            return null;
        }
        
        return userStatusRepository.findById(userStatusId)
                .map(statusRecord -> {
                    UserStatusEnum statusEnum = UserStatusEnum.fromValue(statusRecord.getCode());
                    return new UserStatusResponse()
                            .id(userStatusId)
                            .status(statusEnum);
                })
                .orElse(null);
    }
    
    private List<UserOrganizationMembership> buildOrganizationMemberships(String userId, String userEmail) {
        // Fetch all organization memberships for the user
        List<OrganizationMembersRecord> orgMembers = organizationMemberRepository.findByUserId(userId);
        if (orgMembers.isEmpty()) {
            return List.of();
        }
        
        // Batch fetch all related data
        List<String> organizationIds = orgMembers.stream()
                .map(OrganizationMembersRecord::getOrganizationId)
                .collect(Collectors.toList());
        
        List<String> roleIds = orgMembers.stream()
                .map(OrganizationMembersRecord::getRoleId)
                .filter(roleId -> roleId != null)
                .distinct()
                .collect(Collectors.toList());
        
        // Fetch all organizations, roles, and invitations in batch
        Map<String, OrganizationsRecord> organizationMap = organizationRepository.findByIds(organizationIds)
                .stream()
                .collect(Collectors.toMap(OrganizationsRecord::getId, org -> org));
        
        Map<String, RolesRecord> roleMap = roleRepository.findByIds(roleIds)
                .stream()
                .collect(Collectors.toMap(RolesRecord::getId, role -> role));
        
        // Fetch all invitations for the user's email once
        Map<String, InvitationsRecord> invitationsByOrgId = invitationRepository.findByEmail(userEmail)
                .stream()
                .collect(Collectors.toMap(
                        InvitationsRecord::getOrganizationId,
                        invitation -> invitation,
                        (existing, replacement) -> existing // Keep first if duplicates
                ));
        
        // Fetch invitation statuses if any invitations exist
        Map<String, InvitationStatusesRecord> invitationStatusMap = Map.of();
        if (!invitationsByOrgId.isEmpty()) {
            List<String> invitationStatusIds = invitationsByOrgId.values().stream()
                    .map(InvitationsRecord::getInvitationStatusId)
                    .distinct()
                    .collect(Collectors.toList());
            
            invitationStatusMap = invitationStatusRepository.findByIds(invitationStatusIds)
                    .stream()
                    .collect(Collectors.toMap(InvitationStatusesRecord::getId, status -> status));
        }
        
        // Build organization memberships
        return buildOrganizationMembershipsFromMaps(
                orgMembers, 
                organizationMap, 
                roleMap, 
                invitationsByOrgId, 
                invitationStatusMap
        );
    }
    
    private List<UserOrganizationMembership> buildOrganizationMembershipsFromMaps(
            List<OrganizationMembersRecord> orgMembers,
            Map<String, OrganizationsRecord> organizationMap,
            Map<String, RolesRecord> roleMap,
            Map<String, InvitationsRecord> invitationsByOrgId,
            Map<String, InvitationStatusesRecord> invitationStatusMap) {
        
        List<UserOrganizationMembership> memberships = new java.util.ArrayList<>();
        
        for (OrganizationMembersRecord orgMember : orgMembers) {
            String organizationId = orgMember.getOrganizationId();
            OrganizationsRecord orgRecord = organizationMap.get(organizationId);
            
            if (orgRecord == null) {
                continue; // Skip if organization not found
            }
            
            // Build organization response
            UserOrganizationResponse organization = new UserOrganizationResponse()
                    .id(orgRecord.getId())
                    .subdomain(orgRecord.getSubdomain())
                    .name(orgRecord.getName());
            
            // Build role response
            RoleResponse role = buildRoleResponse(orgMember.getRoleId(), roleMap);
            
            // Build invitation status response
            InvitationStatusResponse invitationStatus = buildInvitationStatusResponse(
                    organizationId, 
                    invitationsByOrgId, 
                    invitationStatusMap
            );
            
            // Create membership
            UserOrganizationMembership membership = new UserOrganizationMembership()
                    .organization(organization)
                    .role(role)
                    .invitationStatus(invitationStatus);
            
            memberships.add(membership);
        }
        
        return memberships;
    }
    
    private RoleResponse buildRoleResponse(String roleId, Map<String, RolesRecord> roleMap) {
        if (roleId == null) {
            return null;
        }
        
        RolesRecord roleRecord = roleMap.get(roleId);
        if (roleRecord == null) {
            return null;
        }
        
        String permissionsValue = PermissionUtil.toPermissionString(roleRecord.getPermissions());
        return new RoleResponse()
                .id(roleRecord.getId())
                .name(roleRecord.getName())
                .description(roleRecord.getDescription())
                .permissionsValue(permissionsValue)
                .createdAt(toOffsetDateTime(roleRecord.getCreatedAt()))
                .updatedAt(toOffsetDateTime(roleRecord.getUpdatedAt()));
    }
    
    private InvitationStatusResponse buildInvitationStatusResponse(
            String organizationId,
            Map<String, InvitationsRecord> invitationsByOrgId,
            Map<String, InvitationStatusesRecord> invitationStatusMap) {
        
        InvitationsRecord invitation = invitationsByOrgId.get(organizationId);
        if (invitation == null) {
            return null;
        }
        
        String invitationStatusId = invitation.getInvitationStatusId();
        InvitationStatusesRecord invitationStatusRecord = invitationStatusMap.get(invitationStatusId);
        
        if (invitationStatusRecord == null) {
            return null;
        }
        
        InvitationStatusEnum invitationStatusEnum = InvitationStatusEnum.fromValue(invitationStatusRecord.getCode());
        return new InvitationStatusResponse()
                .id(invitationStatusId)
                .status(invitationStatusEnum);
    }
    
    private OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atOffset(ZoneOffset.UTC) : null;
    }
}

