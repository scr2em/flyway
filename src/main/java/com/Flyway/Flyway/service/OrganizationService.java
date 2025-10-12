package com.Flyway.Flyway.service;

import com.Flyway.Flyway.dto.request.CreateOrganizationRequest;
import com.Flyway.Flyway.dto.request.UpdateOrganizationRequest;
import com.Flyway.Flyway.dto.response.OrganizationResponse;
import com.Flyway.Flyway.exception.ResourceNotFoundException;
import com.Flyway.Flyway.repository.OrganizationRepository;
import com.Flyway.Flyway.repository.RoleRepository;
import com.Flyway.Flyway.repository.OrganizationMemberRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    
    public OrganizationResponse getOrganizationById(String id) {
        Record org = organizationRepository.findById(id)
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
    
    private OrganizationResponse mapToOrganizationResponse(Record record) {
        return OrganizationResponse.builder()
                .id(record.get("id", String.class))
                .name(record.get("name", String.class))
                .createdBy(record.get("created_by", String.class))
                .createdAt(record.get("created_at", LocalDateTime.class))
                .updatedAt(record.get("updated_at", LocalDateTime.class))
                .build();
    }
}

