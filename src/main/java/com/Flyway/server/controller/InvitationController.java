package com.Flyway.server.controller;

import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.InvitationService;
import com.Flyway.server.dto.generated.InvitationResponse;
import com.Flyway.server.dto.generated.CreateInvitationRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {
    
    private final InvitationService invitationService;
    
    // Private endpoint - requires invitation.create permission
    @PostMapping
    @RequirePermission("invitation.create")
    public ResponseEntity<InvitationResponse> createInvitation(
            @Valid @RequestBody CreateInvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        InvitationResponse invitation = invitationService.createInvitation(
                userDetails.getOrganizationId(),
                request,
                userDetails.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }
    
    // Public endpoint - no authentication required
    @GetMapping("/token/{token}")
    public ResponseEntity<InvitationResponse> getInvitationByToken(@PathVariable String token) {
        InvitationResponse invitation = invitationService.getInvitationByToken(token);
        return ResponseEntity.ok(invitation);
    }
    
    // Public endpoint - no authentication required
    @PostMapping("/token/{token}/accept")
    public ResponseEntity<InvitationResponse> acceptInvitation(@PathVariable String token) {
        InvitationResponse invitation = invitationService.acceptInvitation(token);
        return ResponseEntity.ok(invitation);
    }
    
    // Public endpoint - no authentication required
    @PostMapping("/token/{token}/reject")
    public ResponseEntity<InvitationResponse> rejectInvitation(@PathVariable String token) {
        InvitationResponse invitation = invitationService.rejectInvitation(token);
        return ResponseEntity.ok(invitation);
    }
    
    // Private endpoint - requires invitation.create permission
    @PostMapping("/users/{userId}/resend")
    @RequirePermission("invitation.create")
    public ResponseEntity<InvitationResponse> resendInvitation(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        InvitationResponse invitation = invitationService.resendInvitationByUserId(userId, userDetails.getOrganizationId());
        return ResponseEntity.ok(invitation);
    }
}

