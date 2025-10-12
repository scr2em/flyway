package com.Flyway.Flyway.service;

import com.Flyway.Flyway.dto.request.CreateInvitationRequest;
import com.Flyway.Flyway.dto.response.InvitationResponse;
import com.Flyway.Flyway.exception.BadRequestException;
import com.Flyway.Flyway.exception.ResourceNotFoundException;
import com.Flyway.Flyway.repository.InvitationRepository;
import com.Flyway.Flyway.repository.InvitationStatusRepository;
import com.Flyway.Flyway.repository.OrganizationMemberRepository;
import com.Flyway.Flyway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.Record;
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
    
    public InvitationResponse getInvitationById(String id) {
        Record invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", id));
        
        return mapToInvitationResponse(invitation);
    }
    
    public InvitationResponse getInvitationByToken(String token) {
        Record invitation = invitationRepository.findByToken(token)
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
        Record pendingStatus = invitationStatusRepository.findByCode("pending")
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
                pendingStatus.get("id", String.class),
                token,
                expiresAt
        );
        
        return getInvitationById(invitationId);
    }
    
    @Transactional
    public InvitationResponse acceptInvitation(String token, String userId) {
        Record invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "token", token));
        
        // Check if invitation is pending
        String statusId = invitation.get("invitation_status_id", String.class);
        Record status = invitationStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Status not found"));
        
        if (!"pending".equals(status.get("code", String.class))) {
            throw new BadRequestException("Invitation is not pending");
        }
        
        // Check if expired
        LocalDateTime expiresAt = invitation.get("expires_at", LocalDateTime.class);
        if (expiresAt.isBefore(LocalDateTime.now())) {
            // Update to expired status
            Record expiredStatus = invitationStatusRepository.findByCode("expired")
                    .orElseThrow(() -> new RuntimeException("Expired status not found"));
            invitationRepository.updateStatus(invitation.get("id", String.class), expiredStatus.get("id", String.class));
            throw new BadRequestException("Invitation has expired");
        }
        
        // Verify user email matches invitation email
        Record user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (!user.get("email", String.class).equals(invitation.get("email", String.class))) {
            throw new BadRequestException("User email does not match invitation email");
        }
        
        // Add user to organization
        String organizationId = invitation.get("organization_id", String.class);
        String roleId = invitation.get("role_id", String.class);
        
        if (!memberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            memberRepository.create(organizationId, userId, roleId);
        }
        
        // Update invitation status to accepted
        Record acceptedStatus = invitationStatusRepository.findByCode("accepted")
                .orElseThrow(() -> new RuntimeException("Accepted status not found"));
        invitationRepository.updateStatus(invitation.get("id", String.class), acceptedStatus.get("id", String.class));
        
        return getInvitationById(invitation.get("id", String.class));
    }
    
    @Transactional
    public InvitationResponse rejectInvitation(String token) {
        Record invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "token", token));
        
        // Get rejected status
        Record rejectedStatus = invitationStatusRepository.findByCode("rejected")
                .orElseThrow(() -> new RuntimeException("Rejected status not found"));
        
        // Update invitation status
        invitationRepository.updateStatus(invitation.get("id", String.class), rejectedStatus.get("id", String.class));
        
        return getInvitationById(invitation.get("id", String.class));
    }
    
    @Transactional
    public void deleteInvitation(String id) {
        invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", id));
        
        invitationRepository.delete(id);
    }
    
    private InvitationResponse mapToInvitationResponse(Record record) {
        String statusId = record.get("invitation_status_id", String.class);
        Record statusRecord = invitationStatusRepository.findById(statusId).orElse(null);
        
        return InvitationResponse.builder()
                .id(record.get("id", String.class))
                .organizationId(record.get("organization_id", String.class))
                .email(record.get("email", String.class))
                .roleId(record.get("role_id", String.class))
                .invitedBy(record.get("invited_by", String.class))
                .invitationStatusId(statusId)
                .invitationStatusCode(statusRecord != null ? statusRecord.get("code", String.class) : null)
                .invitationStatusLabel(statusRecord != null ? statusRecord.get("label", String.class) : null)
                .token(record.get("token", String.class))
                .expiresAt(record.get("expires_at", LocalDateTime.class))
                .respondedAt(record.get("responded_at", LocalDateTime.class))
                .createdAt(record.get("created_at", LocalDateTime.class))
                .updatedAt(record.get("updated_at", LocalDateTime.class))
                .build();
    }
}

