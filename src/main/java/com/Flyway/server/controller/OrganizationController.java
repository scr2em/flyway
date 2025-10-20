package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.CreateOrganizationRequest;
import com.Flyway.server.dto.generated.UpdateOrganizationRequest;
import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.OrganizationService;
import com.Flyway.server.dto.generated.OrganizationResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    
    private final OrganizationService organizationService;
    
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getCurrentUserOrganizations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        List<OrganizationResponse> organizations = organizationService.getCurrentUserOrganizations(userDetails.getId());
        return ResponseEntity.ok(organizations);
    }
    
    @GetMapping("/subdomain/{subdomain}")
    public ResponseEntity<OrganizationResponse> getOrganizationBySubdomain(
            @PathVariable String subdomain,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
                
        OrganizationResponse organization = organizationService.getOrganizationBySubdomain(subdomain, userDetails.getId());
        return ResponseEntity.ok(organization);
    }

    
    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrganizationResponse organization = organizationService.createOrganization(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(organization);
    }
    
    @PutMapping
    @RequirePermission("organization.update")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @Valid @RequestBody UpdateOrganizationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrganizationResponse organization = organizationService.updateOrganization(
                userDetails.getOrganizationId(), request, userDetails.getId());
        return ResponseEntity.ok(organization);
    }
}

