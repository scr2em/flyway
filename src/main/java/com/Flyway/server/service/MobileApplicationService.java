package com.Flyway.server.service;

import com.Flyway.server.dto.generated.CreateMobileApplicationRequest;
import com.Flyway.server.dto.generated.UpdateMobileApplicationRequest;
import com.Flyway.server.dto.generated.MobileApplicationResponse;
import com.Flyway.server.event.MobileAppCreatedEvent;
import com.Flyway.server.event.MobileAppDeletedEvent;
import com.Flyway.server.event.MobileAppUpdatedEvent;
import com.Flyway.server.jooq.tables.records.MobileApplicationsRecord;
import com.Flyway.server.exception.ConflictException;
import com.Flyway.server.exception.ForbiddenException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.repository.MobileApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobileApplicationService {
    
    private final MobileApplicationRepository mobileApplicationRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public MobileApplicationResponse getMobileApplicationById(String id, String organizationId) {
        MobileApplicationsRecord app = mobileApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mobile Application", "id", id));
        
        // Verify the app belongs to the user's organization
        if (!app.getOrganizationId().equals(organizationId)) {
            throw new ForbiddenException("You do not have access to this mobile application");
        }
        
        return mapToMobileApplicationResponse(app);
    }
    
    public MobileApplicationResponse getMobileApplicationByBundleId(String bundleId, String organizationId) {
        MobileApplicationsRecord app = mobileApplicationRepository.findByBundleId(bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Mobile Application", "bundleId", bundleId));
        
        // Verify the app belongs to the user's organization
        if (!app.getOrganizationId().equals(organizationId)) {
            throw new ForbiddenException("You do not have access to this mobile application");
        }
        
        return mapToMobileApplicationResponse(app);
    }
    
    public List<MobileApplicationResponse> getAllMobileApplications(String organizationId) {
        return mobileApplicationRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToMobileApplicationResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public MobileApplicationResponse createMobileApplication(
            CreateMobileApplicationRequest request, 
            String organizationId, 
            String userId) {
        
        // Check if bundle ID already exists
        if (mobileApplicationRepository.existsByBundleId(request.getBundleId())) {
            throw new ConflictException("A mobile application with bundle ID '" + request.getBundleId() + "' already exists");
        }
        
        // Check if name already exists for this organization
        if (mobileApplicationRepository.existsByNameAndOrganizationId(request.getName(), organizationId)) {
            throw new ConflictException("A mobile application with name '" + request.getName() + "' already exists in this organization");
        }
        
        // Create the mobile application
        String appId = mobileApplicationRepository.create(
                request.getBundleId(),
                organizationId,
                request.getName(),
                request.getDescription(),
                userId
        );
        
        MobileApplicationResponse response = getMobileApplicationById(appId, organizationId);
        
        // Publish event for audit logging, webhooks, and notifications
        eventPublisher.publishEvent(new MobileAppCreatedEvent(
                appId,
                request.getName(),
                request.getBundleId(),
                userId,
                organizationId
        ));
        
        return response;
    }
    
    @Transactional
    public MobileApplicationResponse updateMobileApplication(
            String id, 
            UpdateMobileApplicationRequest request, 
            String organizationId,
            String userId) {
        
        MobileApplicationsRecord app = mobileApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mobile Application", "id", id));
        
        // Verify the app belongs to the user's organization
        if (!app.getOrganizationId().equals(organizationId)) {
            throw new ForbiddenException("You do not have access to this mobile application");
        }
        
        // Check if the new name already exists for this organization (but not for this app)
        if (!app.getName().equals(request.getName()) && 
            mobileApplicationRepository.existsByNameAndOrganizationId(request.getName(), organizationId)) {
            throw new ConflictException("A mobile application with name '" + request.getName() + "' already exists in this organization");
        }
        
        // Update the mobile application
        mobileApplicationRepository.update(id, request.getName(), request.getDescription());
        
        MobileApplicationResponse response = getMobileApplicationById(id, organizationId);
        
        // Publish event for audit logging, webhooks, and notifications
        eventPublisher.publishEvent(new MobileAppUpdatedEvent(
                id,
                request.getName(),
                app.getBundleId(),
                userId,
                organizationId
        ));
        
        return response;
    }
    
    @Transactional
    public void deleteMobileApplication(String id, String organizationId, String userId) {
        MobileApplicationsRecord app = mobileApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mobile Application", "id", id));
        
        // Verify the app belongs to the user's organization
        if (!app.getOrganizationId().equals(organizationId)) {
            throw new ForbiddenException("You do not have access to this mobile application");
        }
        
        // Store app details before deletion for the event
        String appName = app.getName();
        String bundleId = app.getBundleId();
        
        mobileApplicationRepository.delete(id);
        
        // Publish event for audit logging, webhooks, and notifications
        eventPublisher.publishEvent(new MobileAppDeletedEvent(
                id,
                appName,
                bundleId,
                userId,
                organizationId
        ));
    }
    
    private MobileApplicationResponse mapToMobileApplicationResponse(MobileApplicationsRecord record) {
        LocalDateTime createdAtLocal = record.getCreatedAt();
        LocalDateTime updatedAtLocal = record.getUpdatedAt();
        
        return new MobileApplicationResponse()
                .id(record.getId())
                .bundleId(record.getBundleId())
                .organizationId(record.getOrganizationId())
                .name(record.getName())
                .description(record.getDescription())
                .createdBy(record.getCreatedBy())
                .createdAt(createdAtLocal != null ? createdAtLocal.atOffset(ZoneOffset.UTC) : null)
                .updatedAt(updatedAtLocal != null ? updatedAtLocal.atOffset(ZoneOffset.UTC) : null);
    }
}

