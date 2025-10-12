package com.Flyway.server.service;

import com.Flyway.server.dto.generated.CreateInvitationRequest;
import com.Flyway.server.dto.generated.InvitationResponse;
import com.Flyway.server.dto.generated.InvitationStatusResponse;
import com.Flyway.server.dto.generated.InvitationStatusEnum;
import com.Flyway.server.dto.generated.OrganizationResponse;
import com.Flyway.server.dto.generated.RoleResponse;
import com.Flyway.server.dto.generated.UserResponse;

import java.time.ZoneOffset;

import com.Flyway.server.jooq.tables.records.InvitationsRecord;
import com.Flyway.server.jooq.tables.records.InvitationStatusesRecord;
import com.Flyway.server.jooq.tables.records.UsersRecord;
import com.Flyway.server.exception.BadRequestException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.repository.InvitationRepository;
import com.Flyway.server.repository.InvitationStatusRepository;
import com.Flyway.server.repository.OrganizationMemberRepository;
import com.Flyway.server.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationService {
    
    private final InvitationRepository invitationRepository;
    private final InvitationStatusRepository invitationStatusRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final OrganizationService organizationService;
    private final RoleService roleService;
    private final UserService userService;
    
    public InvitationResponse getInvitationById(String id) {
        InvitationsRecord invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", id));
        
        return mapToInvitationResponse(invitation);
    }
    
    public InvitationResponse getInvitationByToken(String token) {
        InvitationsRecord invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "token", token));
        
        return mapToInvitationResponse(invitation);
    }
    
    public List<InvitationResponse> getInvitationsByOrganizationId(String organizationId) {
        return invitationRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToInvitationResponse)
                .collect(Collectors.toList());
    }
    
    public List<InvitationResponse> getInvitationsByEmail(String email) {
        return invitationRepository.findByEmail(email).stream()
                .map(this::mapToInvitationResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public InvitationResponse createInvitation(String organizationId, CreateInvitationRequest request, String invitedBy) {
        // Get pending status
        InvitationStatusesRecord pendingStatus = invitationStatusRepository.findByCode("pending")
                .orElseThrow(() -> new RuntimeException("Pending status not found"));
        
        // Generate unique token
        String token = UUID.randomUUID().toString();
        
        // Set expiration (7 days from now)
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        
        // Create invitation
        String invitationId = invitationRepository.create(
                organizationId,
                request.getEmail(),
                request.getRoleId(),
                invitedBy,
                pendingStatus.getId(),
                token,
                expiresAt
        );
        
        return getInvitationById(invitationId);
    }
    
    @Transactional
    public InvitationResponse acceptInvitation(String token, String userId) {
        InvitationsRecord invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "token", token));
        
        // Check if invitation is pending
        String statusId = invitation.getInvitationStatusId();
        InvitationStatusesRecord status = invitationStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Status not found"));
        
        if (!"pending".equals(status.getCode())) {
            throw new BadRequestException("Invitation is not pending");
        }
        
        // Check if expired
        LocalDateTime expiresAt = invitation.getExpiresAt();
        if (expiresAt.isBefore(LocalDateTime.now())) {
            // Update to expired status
            InvitationStatusesRecord expiredStatus = invitationStatusRepository.findByCode("expired")
                    .orElseThrow(() -> new RuntimeException("Expired status not found"));
            invitationRepository.updateStatus(invitation.getId(), expiredStatus.getId());
            throw new BadRequestException("Invitation has expired");
        }
        
        // Verify user email matches invitation email
        UsersRecord user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (!user.getEmail().equals(invitation.getEmail())) {
            throw new BadRequestException("User email does not match invitation email");
        }
        
        // Add user to organization
        String organizationId = invitation.getOrganizationId();
        String roleId = invitation.getRoleId();
        
        if (!memberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            memberRepository.create(organizationId, userId, roleId);
        }
        
        // Update invitation status to accepted
        InvitationStatusesRecord acceptedStatus = invitationStatusRepository.findByCode("accepted")
                .orElseThrow(() -> new RuntimeException("Accepted status not found"));
        invitationRepository.updateStatus(invitation.getId(), acceptedStatus.getId());
        
        return getInvitationById(invitation.getId());
    }
    
    @Transactional
    public InvitationResponse rejectInvitation(String token) {
        InvitationsRecord invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "token", token));
        
        // Get rejected status
        InvitationStatusesRecord rejectedStatus = invitationStatusRepository.findByCode("rejected")
                .orElseThrow(() -> new RuntimeException("Rejected status not found"));
        
        // Update invitation status
        invitationRepository.updateStatus(invitation.getId(), rejectedStatus.getId());
        
        return getInvitationById(invitation.getId());
    }
    
    @Transactional
    public void deleteInvitation(String id) {
        invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", id));
        
        invitationRepository.delete(id);
    }
    
    private InvitationResponse mapToInvitationResponse(InvitationsRecord record) {
        String statusId = record.getInvitationStatusId();
        String organizationId = record.getOrganizationId();
        String roleId = record.getRoleId();
        String invitedById = record.getInvitedBy();
        
        // Fetch nested objects
        InvitationStatusesRecord statusRecord = invitationStatusRepository.findById(statusId).orElse(null);
        OrganizationResponse organization = organizationService.getOrganizationById(organizationId);
        RoleResponse role = roleService.getRoleById(roleId);
        UserResponse invitedBy = invitedById != null ? userService.getUserById(invitedById) : null;
        
        // Create InvitationStatusResponse
        InvitationStatusResponse statusResponse = null;
        if (statusRecord != null) {
            String statusCode = statusRecord.getCode();
            InvitationStatusEnum statusEnum = InvitationStatusEnum.fromValue(statusCode);
            statusResponse = new InvitationStatusResponse()
                    .id(statusId)
                    .status(statusEnum);
        }
        
        // Convert LocalDateTime to OffsetDateTime
        LocalDateTime createdAtLocal = record.getCreatedAt();
        LocalDateTime expiresAtLocal = record.getExpiresAt();
        
        return new InvitationResponse()
                .id(record.getId())
                .email(record.getEmail())
                .organization(organization)
                .role(role)
                .status(statusResponse)
                .invitedBy(invitedBy)
                .createdAt(createdAtLocal != null ? createdAtLocal.atOffset(ZoneOffset.UTC) : null)
                .expiresAt(expiresAtLocal != null ? expiresAtLocal.atOffset(ZoneOffset.UTC) : null);
    }
}

