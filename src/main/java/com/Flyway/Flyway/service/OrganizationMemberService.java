package com.Flyway.Flyway.service;

import com.Flyway.Flyway.dto.request.AddOrganizationMemberRequest;
import com.Flyway.Flyway.dto.request.UpdateMemberRoleRequest;
import com.Flyway.Flyway.dto.response.OrganizationMemberResponse;
import com.Flyway.Flyway.dto.response.RoleResponse;
import com.Flyway.Flyway.dto.response.UserResponse;
import com.Flyway.Flyway.exception.ConflictException;
import com.Flyway.Flyway.exception.ResourceNotFoundException;
import com.Flyway.Flyway.repository.OrganizationMemberRepository;
import com.Flyway.Flyway.repository.RoleRepository;
import com.Flyway.Flyway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        Record member = memberRepository.findById(id)
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
        Record role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
        
        if (!role.get("organization_id", String.class).equals(organizationId)) {
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
        Record member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization member", "id", memberId));
        
        // Check if role exists and belongs to same organization
        Record role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
        
        String orgId = member.get("organization_id", String.class);
        if (!role.get("organization_id", String.class).equals(orgId)) {
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
    
    private OrganizationMemberResponse mapToMemberResponse(Record record) {
        String userId = record.get("user_id", String.class);
        String roleId = record.get("role_id", String.class);
        
        UserResponse user = userService.getUserById(userId);
        RoleResponse role = roleService.getRoleById(roleId);
        
        return OrganizationMemberResponse.builder()
                .id(record.get("id", String.class))
                .organizationId(record.get("organization_id", String.class))
                .userId(userId)
                .roleId(roleId)
                .user(user)
                .role(role)
                .joinedAt(record.get("joined_at", LocalDateTime.class))
                .createdAt(record.get("created_at", LocalDateTime.class))
                .build();
    }
}

