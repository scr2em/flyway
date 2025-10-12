package com.Flyway.Flyway.controller;

import com.Flyway.Flyway.dto.request.CreateOrganizationRequest;
import com.Flyway.Flyway.dto.request.UpdateOrganizationRequest;
import com.Flyway.Flyway.dto.response.ApiResponse;
import com.Flyway.Flyway.dto.response.OrganizationResponse;
import com.Flyway.Flyway.security.CustomUserDetails;
import com.Flyway.Flyway.security.RequirePermission;
import com.Flyway.Flyway.service.OrganizationService;
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
    @RequirePermission("organization.view")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getCurrentOrganization(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrganizationResponse organization = organizationService.getOrganizationById(userDetails.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(organization));
    }
    
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getAllOrganizations() {
        List<OrganizationResponse> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(ApiResponse.success(organizations));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrganizationResponse organization = organizationService.createOrganization(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Organization created successfully", organization));
    }
    
    @PutMapping
    @RequirePermission("organization.update")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @Valid @RequestBody UpdateOrganizationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrganizationResponse organization = organizationService.updateOrganization(userDetails.getOrganizationId(), request);
        return ResponseEntity.ok(ApiResponse.success("Organization updated successfully", organization));
    }
    
    @DeleteMapping
    @RequirePermission("organization.delete")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        organizationService.deleteOrganization(userDetails.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success("Organization deleted successfully", null));
    }
}

