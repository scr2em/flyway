package com.Flyway.server.service;

import com.Flyway.server.dto.generated.AddOrganizationMemberRequest;
import com.Flyway.server.dto.generated.UpdateMemberRoleRequest;
import com.Flyway.server.dto.generated.OrganizationMemberResponse;
import com.Flyway.server.dto.generated.RoleResponse;
import com.Flyway.server.dto.generated.UserResponse;
import com.Flyway.server.jooq.tables.records.OrganizationMembersRecord;
import com.Flyway.server.jooq.tables.records.RolesRecord;
import com.Flyway.server.exception.ConflictException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.repository.OrganizationMemberRepository;
import com.Flyway.server.repository.RoleRepository;
import com.Flyway.server.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationMemberService {
    
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final RoleService roleService;
    
    public OrganizationMemberResponse getMemberById(String id) {
        OrganizationMembersRecord member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization member", "id", id));
        
        return mapToMemberResponse(member);
    }
    
    public List<OrganizationMemberResponse> getMembersByOrganizationId(String organizationId) {
        return memberRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }
    
    public List<OrganizationMemberResponse> getMembersByUserId(String userId) {
        return memberRepository.findByUserId(userId).stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public OrganizationMemberResponse addMember(String organizationId, AddOrganizationMemberRequest request) {
        // Check if user exists
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        
        // Check if role exists and belongs to organization
        RolesRecord role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
        
        if (!role.getOrganizationId().equals(organizationId)) {
            throw new ConflictException("Role does not belong to this organization");
        }
        
        // Check if user is already a member
        if (memberRepository.existsByOrganizationIdAndUserId(organizationId, request.getUserId())) {
            throw new ConflictException("User is already a member of this organization");
        }
        
        // Add member
        String memberId = memberRepository.create(organizationId, request.getUserId(), request.getRoleId());
        
        return getMemberById(memberId);
    }
    
    @Transactional
    public OrganizationMemberResponse updateMemberRole(String memberId, UpdateMemberRoleRequest request) {
        OrganizationMembersRecord member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization member", "id", memberId));
        
        // Check if role exists and belongs to same organization
        RolesRecord role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
        
        String orgId = member.getOrganizationId();
        if (!role.getOrganizationId().equals(orgId)) {
            throw new ConflictException("Role does not belong to this organization");
        }
        
        // Update role
        memberRepository.updateRole(memberId, request.getRoleId());
        
        return getMemberById(memberId);
    }
    
    @Transactional
    public void removeMember(String memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization member", "id", memberId));
        
        memberRepository.delete(memberId);
    }
    
    private OrganizationMemberResponse mapToMemberResponse(OrganizationMembersRecord record) {
        String userId = record.getUserId();
        String roleId = record.getRoleId();
        
        UserResponse user = userService.getUserById(userId);
        RoleResponse role = roleService.getRoleById(roleId);
        
        // Convert LocalDateTime to OffsetDateTime
        LocalDateTime joinedAtLocal = record.getJoinedAt();
        
        return new OrganizationMemberResponse()
                .id(record.getId())
                .user(user)
                .role(role)
                .joinedAt(joinedAtLocal != null ? joinedAtLocal.atOffset(ZoneOffset.UTC) : null);
    }
}

