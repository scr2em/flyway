package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.CreateMobileApplicationRequest;
import com.Flyway.server.dto.generated.UpdateMobileApplicationRequest;
import com.Flyway.server.dto.generated.MobileApplicationResponse;
import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.MobileApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile-applications")
@RequiredArgsConstructor
public class MobileApplicationController {
    
    private final MobileApplicationService mobileApplicationService;
    
    @GetMapping
    @RequirePermission("mobile_app.read")
    public ResponseEntity<List<MobileApplicationResponse>> getAllMobileApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MobileApplicationResponse> apps = mobileApplicationService.getAllMobileApplications(
                userDetails.getOrganizationId());
        return ResponseEntity.ok(apps);
    }
    
    @GetMapping("/{id}")
    @RequirePermission("mobile_app.read")
    public ResponseEntity<MobileApplicationResponse> getMobileApplicationById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MobileApplicationResponse app = mobileApplicationService.getMobileApplicationById(
                id, userDetails.getOrganizationId());
        return ResponseEntity.ok(app);
    }
    
    @GetMapping("/bundle/{bundleId}")
    @RequirePermission("mobile_app.read")
    public ResponseEntity<MobileApplicationResponse> getMobileApplicationByBundleId(
            @PathVariable String bundleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MobileApplicationResponse app = mobileApplicationService.getMobileApplicationByBundleId(
                bundleId, userDetails.getOrganizationId());
        return ResponseEntity.ok(app);
    }
    
    @PostMapping
    @RequirePermission("mobile_app.create")
    public ResponseEntity<MobileApplicationResponse> createMobileApplication(
            @Valid @RequestBody CreateMobileApplicationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MobileApplicationResponse app = mobileApplicationService.createMobileApplication(
                request, userDetails.getOrganizationId(), userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(app);
    }
    
    @PutMapping("/{id}")
    @RequirePermission("mobile_app.update")
    public ResponseEntity<MobileApplicationResponse> updateMobileApplication(
            @PathVariable String id,
            @Valid @RequestBody UpdateMobileApplicationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MobileApplicationResponse app = mobileApplicationService.updateMobileApplication(
                id, request, userDetails.getOrganizationId(), userDetails.getId());
        return ResponseEntity.ok(app);
    }
    
    @DeleteMapping("/{id}")
    @RequirePermission("mobile_app.delete")
    public ResponseEntity<Void> deleteMobileApplication(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        mobileApplicationService.deleteMobileApplication(id, userDetails.getOrganizationId(), userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}

