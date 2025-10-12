package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.CreateInvitationRequest;
import com.Flyway.server.dto.generated.RespondToInvitationRequest;
import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.InvitationService;
import com.Flyway.server.dto.generated.InvitationResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {
    
    private final InvitationService invitationService;
    
    @GetMapping("/{id}")
    @RequirePermission("invitation.view")
    public ResponseEntity<InvitationResponse> getInvitationById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Verify invitation belongs to user's organization
        invitationService.verifyInvitationOwnership(id, userDetails.getOrganizationId());
        InvitationResponse invitation = invitationService.getInvitationById(id);
        return ResponseEntity.ok(invitation);
    }
    
    @GetMapping("/token/{token}")
    public ResponseEntity<InvitationResponse> getInvitationByToken(
            @PathVariable String token,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Verify invitation is addressed to the current user's email
        invitationService.verifyInvitationRecipient(token, userDetails.getEmail());
        InvitationResponse invitation = invitationService.getInvitationByToken(token);
        return ResponseEntity.ok(invitation);
    }
    
    @GetMapping
    @RequirePermission("invitation.view")
    public ResponseEntity<List<InvitationResponse>> getInvitationsByOrganization(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<InvitationResponse> invitations = invitationService.getInvitationsByOrganizationId(userDetails.getOrganizationId());
        return ResponseEntity.ok(invitations);
    }
    
    @GetMapping("/my-invitations")
    public ResponseEntity<List<InvitationResponse>> getMyInvitations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Users can view invitations sent to their email
        List<InvitationResponse> invitations = invitationService.getInvitationsByEmail(userDetails.getEmail());
        return ResponseEntity.ok(invitations);
    }
    
    @PostMapping
    @RequirePermission("invitation.create")
    public ResponseEntity<InvitationResponse> createInvitation(
            @Valid @RequestBody CreateInvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        InvitationResponse invitation = invitationService.createInvitation(
                userDetails.getOrganizationId(), request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }
    
    @PostMapping("/token/{token}/respond")
    public ResponseEntity<InvitationResponse> respondToInvitation(
            @PathVariable String token,
            @Valid @RequestBody RespondToInvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Verify invitation is addressed to the current user's email
        invitationService.verifyInvitationRecipient(token, userDetails.getEmail());
        
        InvitationResponse invitation;
        if (request.getAccept()) {
            invitation = invitationService.acceptInvitation(token, userDetails.getId());
        } else {
            invitation = invitationService.rejectInvitation(token);
        }
        return ResponseEntity.ok(invitation);
    }
    
    @DeleteMapping("/{id}")
    @RequirePermission("invitation.delete")
    public ResponseEntity<Void> deleteInvitation(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Verify invitation belongs to user's organization
        invitationService.verifyInvitationOwnership(id, userDetails.getOrganizationId());
        invitationService.deleteInvitation(id);
        return ResponseEntity.ok().build();
    }
}

