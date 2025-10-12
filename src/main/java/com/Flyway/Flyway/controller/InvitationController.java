package com.Flyway.Flyway.controller;

import com.Flyway.Flyway.dto.request.CreateInvitationRequest;
import com.Flyway.Flyway.dto.request.RespondToInvitationRequest;
import com.Flyway.Flyway.dto.response.ApiResponse;
import com.Flyway.Flyway.dto.response.InvitationResponse;
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
    public ResponseEntity<ApiResponse<InvitationResponse>> getInvitationById(@PathVariable String id) {
        InvitationResponse invitation = invitationService.getInvitationById(id);
        return ResponseEntity.ok(ApiResponse.success(invitation));
    }
    
    @GetMapping("/token/{token}")
    public ResponseEntity<ApiResponse<InvitationResponse>> getInvitationByToken(@PathVariable String token) {
        InvitationResponse invitation = invitationService.getInvitationByToken(token);
        return ResponseEntity.ok(ApiResponse.success(invitation));
    }
    
    @GetMapping
    @RequirePermission("invitation.view")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getInvitationsByOrganization(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<InvitationResponse> invitations = invitationService.getInvitationsByOrganizationId(userDetails.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(invitations));
    }
    
    @GetMapping("/my-invitations")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getMyInvitations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Assuming user email is stored in the user details
        List<InvitationResponse> invitations = invitationService.getInvitationsByEmail(userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(invitations));
    }
    
    @PostMapping
    @RequirePermission("invitation.create")
    public ResponseEntity<ApiResponse<InvitationResponse>> createInvitation(
            @Valid @RequestBody CreateInvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        InvitationResponse invitation = invitationService.createInvitation(
                userDetails.getOrganizationId(), request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invitation created successfully", invitation));
    }
    
    @PostMapping("/token/{token}/respond")
    public ResponseEntity<ApiResponse<InvitationResponse>> respondToInvitation(
            @PathVariable String token,
            @Valid @RequestBody RespondToInvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        InvitationResponse invitation;
        if ("accept".equals(request.getResponse())) {
            invitation = invitationService.acceptInvitation(token, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Invitation accepted successfully", invitation));
        } else {
            invitation = invitationService.rejectInvitation(token);
            return ResponseEntity.ok(ApiResponse.success("Invitation rejected successfully", invitation));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvitation(@PathVariable String id) {
        invitationService.deleteInvitation(id);
        return ResponseEntity.ok(ApiResponse.success("Invitation deleted successfully", null));
    }
}

