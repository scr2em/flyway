package com.Flyway.Flyway.controller;

import com.Flyway.Flyway.dto.generated.CreateInvitationRequest;
import com.Flyway.Flyway.dto.generated.RespondToInvitationRequest;
import com.Flyway.Flyway.dto.generated.InvitationResponse;
import com.Flyway.Flyway.security.CustomUserDetails;
import com.Flyway.Flyway.security.RequirePermission;
import com.Flyway.Flyway.service.InvitationService;
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
    public ResponseEntity<InvitationResponse> getInvitationById(@PathVariable String id) {
        InvitationResponse invitation = invitationService.getInvitationById(id);
        return ResponseEntity.ok(invitation);
    }
    
    @GetMapping("/token/{token}")
    public ResponseEntity<InvitationResponse> getInvitationByToken(@PathVariable String token) {
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
        // Assuming user email is stored in the user details
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
        InvitationResponse invitation;
        if (request.getAccept()) {
            invitation = invitationService.acceptInvitation(token, userDetails.getId());
        } else {
            invitation = invitationService.rejectInvitation(token);
        }
        return ResponseEntity.ok(invitation);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvitation(@PathVariable String id) {
        invitationService.deleteInvitation(id);
        return ResponseEntity.ok().build();
    }
}

