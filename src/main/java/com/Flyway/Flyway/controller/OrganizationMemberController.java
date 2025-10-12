package com.Flyway.Flyway.controller;

import com.Flyway.Flyway.dto.request.AddOrganizationMemberRequest;
import com.Flyway.Flyway.dto.request.UpdateMemberRoleRequest;
import com.Flyway.Flyway.dto.response.ApiResponse;
import com.Flyway.Flyway.dto.response.OrganizationMemberResponse;
import com.Flyway.Flyway.security.CustomUserDetails;
import com.Flyway.Flyway.security.RequirePermission;
import com.Flyway.Flyway.service.OrganizationMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class OrganizationMemberController {
    
    private final OrganizationMemberService memberService;
    
    @GetMapping("/{memberId}")
    @RequirePermission("member.view")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> getMemberById(
            @PathVariable String memberId) {
        OrganizationMemberResponse member = memberService.getMemberById(memberId);
        return ResponseEntity.ok(ApiResponse.success(member));
    }
    
    @GetMapping
    @RequirePermission("member.view")
    public ResponseEntity<ApiResponse<List<OrganizationMemberResponse>>> getMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrganizationMemberResponse> members = memberService.getMembersByOrganizationId(userDetails.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(members));
    }
    
    @PostMapping
    @RequirePermission("member.add")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> addMember(
            @Valid @RequestBody AddOrganizationMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrganizationMemberResponse member = memberService.addMember(userDetails.getOrganizationId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", member));
    }
    
    @PutMapping("/{memberId}")
    @RequirePermission("member.update_role")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> updateMemberRole(
            @PathVariable String memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        OrganizationMemberResponse member = memberService.updateMemberRole(memberId, request);
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", member));
    }
    
    @DeleteMapping("/{memberId}")
    @RequirePermission("member.remove")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable String memberId) {
        memberService.removeMember(memberId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
}

