package com.Flyway.server.service;

import com.Flyway.server.dto.generated.CreateOrganizationRequest;
import com.Flyway.server.dto.generated.UpdateOrganizationRequest;
import com.Flyway.server.dto.generated.OrganizationResponse;
import com.Flyway.server.jooq.tables.records.OrganizationsRecord;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.repository.OrganizationMemberRepository;
import com.Flyway.server.repository.OrganizationRepository;
import com.Flyway.server.repository.RoleRepository;

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
        // Create organization
        String orgId = organizationRepository.create(request.getName(), createdBy);
        
        // Create Owner role (system role, immutable)
        String ownerRoleId = roleRepository.create(orgId, "Owner", true, true);
        
        // Add creator as member with Owner role
        organizationMemberRepository.create(orgId, createdBy, ownerRoleId);
        
        return getOrganizationById(orgId);
    }
    
    @Transactional
    public OrganizationResponse updateOrganization(String id, UpdateOrganizationRequest request) {
        organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        organizationRepository.update(id, request.getName());
        return getOrganizationById(id);
    }
    
    @Transactional
    public void deleteOrganization(String id) {
        organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        organizationRepository.delete(id);
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

