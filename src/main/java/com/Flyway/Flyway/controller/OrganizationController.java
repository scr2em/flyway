package com.Flyway.Flyway.controller;

import com.Flyway.Flyway.dto.request.CreateOrganizationRequest;
import com.Flyway.Flyway.dto.request.UpdateOrganizationRequest;
import com.Flyway.Flyway.dto.response.ApiResponse;
import com.Flyway.Flyway.dto.response.OrganizationResponse;
import com.Flyway.Flyway.security.CustomUserDetails;
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
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(@PathVariable String id) {
        OrganizationResponse organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(ApiResponse.success(organization));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getAllOrganizations() {
        List<OrganizationResponse> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(ApiResponse.success(organizations));
    }
    
    @GetMapping("/my-organizations")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getMyOrganizations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrganizationResponse> organizations = organizationService.getOrganizationsByCreator(userDetails.getId());
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
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        OrganizationResponse organization = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.success("Organization updated successfully", organization));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(@PathVariable String id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(ApiResponse.success("Organization deleted successfully", null));
    }
}

