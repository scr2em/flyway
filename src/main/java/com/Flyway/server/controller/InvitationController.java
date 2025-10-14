package com.Flyway.server.controller;

import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.InvitationService;
import com.Flyway.server.dto.generated.InvitationResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {
    
    private final InvitationService invitationService;
    
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
    @PostMapping("/{id}/resend")
    @RequirePermission("invitation.create")
    public ResponseEntity<InvitationResponse> resendInvitation(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Verify invitation belongs to user's organization
        invitationService.verifyInvitationOwnership(id, userDetails.getOrganizationId());
        InvitationResponse invitation = invitationService.resendInvitation(id);
        return ResponseEntity.ok(invitation);
    }
}

