package com.Flyway.server.service;

import com.Flyway.server.dto.generated.CreateOrganizationRequest;
import com.Flyway.server.dto.generated.UpdateOrganizationRequest;
import com.Flyway.server.dto.generated.OrganizationResponse;
import com.Flyway.server.jooq.tables.records.OrganizationsRecord;
import com.Flyway.server.exception.ConflictException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.repository.OrganizationMemberRepository;
import com.Flyway.server.repository.OrganizationRepository;
import com.Flyway.server.repository.RoleRepository;
import com.Flyway.server.util.PermissionUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    
    public OrganizationResponse getOrganizationById(String id) {
        OrganizationsRecord org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        return mapToOrganizationResponse(org);
    }
    
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::mapToOrganizationResponse)
                .collect(Collectors.toList());
    }
    
    public List<OrganizationResponse> getOrganizationsByCreator(String createdBy) {
        return organizationRepository.findByCreatedBy(createdBy).stream()
                .map(this::mapToOrganizationResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request, String createdBy) {
        // Check if user has already created an organization
        // Rule: users can only create 1 organization
        if (!organizationRepository.findByCreatedBy(createdBy).isEmpty()) {
            throw new ConflictException("You have already created an organization. Users can only create one organization.");
        }
        
        // Check if user is already a member of an organization
        // Rule: users can only be in 1 organization
        if (!organizationMemberRepository.findByUserId(createdBy).isEmpty()) {
            throw new ConflictException("You are already a member of an organization. Users can only be in one organization.");
        }
        
        // Create organization
        String orgId = organizationRepository.create(request.getName(), createdBy);
        
        // Assign Owner role to creator
        assignOwnerRole(orgId, createdBy);
        
        return getOrganizationById(orgId);
    }
    
    @Transactional
    public OrganizationResponse updateOrganization(String id, UpdateOrganizationRequest request) {
        organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        organizationRepository.update(id, request.getName());
        return getOrganizationById(id);
    }
    
    /**
     * Assigns the Owner role from global roles to the organization creator
     */
    private void assignOwnerRole(String orgId, String createdBy) {
        // Find the global Owner role
        String ownerRoleId = roleRepository.findByName("Owner")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "Owner"))
                .getId();
        
        // Add creator as member with Owner role
        organizationMemberRepository.create(orgId, createdBy, ownerRoleId);
    }
    
    private OrganizationResponse mapToOrganizationResponse(OrganizationsRecord record) {
        // Convert LocalDateTime to OffsetDateTime
        LocalDateTime createdAtLocal = record.getCreatedAt();
        LocalDateTime updatedAtLocal = record.getUpdatedAt();
        
        return new OrganizationResponse()
                .id(record.getId())
                .name(record.getName())
                .description(null) // Description not stored in database
                .createdAt(createdAtLocal != null ? createdAtLocal.atOffset(ZoneOffset.UTC) : null)
                .updatedAt(updatedAtLocal != null ? updatedAtLocal.atOffset(ZoneOffset.UTC) : null);
    }
}

