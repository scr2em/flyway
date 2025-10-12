package com.Flyway.Flyway.controller;

import com.Flyway.Flyway.dto.request.AddOrganizationMemberRequest;
import com.Flyway.Flyway.dto.request.UpdateMemberRoleRequest;
import com.Flyway.Flyway.dto.response.ApiResponse;
import com.Flyway.Flyway.dto.response.OrganizationMemberResponse;
import com.Flyway.Flyway.service.OrganizationMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{organizationId}/members")
@RequiredArgsConstructor
public class OrganizationMemberController {
    
    private final OrganizationMemberService memberService;
    
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> getMemberById(
            @PathVariable String organizationId,
            @PathVariable String memberId) {
        OrganizationMemberResponse member = memberService.getMemberById(memberId);
        return ResponseEntity.ok(ApiResponse.success(member));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationMemberResponse>>> getMembers(
            @PathVariable String organizationId) {
        List<OrganizationMemberResponse> members = memberService.getMembersByOrganizationId(organizationId);
        return ResponseEntity.ok(ApiResponse.success(members));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> addMember(
            @PathVariable String organizationId,
            @Valid @RequestBody AddOrganizationMemberRequest request) {
        OrganizationMemberResponse member = memberService.addMember(organizationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", member));
    }
    
    @PutMapping("/{memberId}")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> updateMemberRole(
            @PathVariable String organizationId,
            @PathVariable String memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        OrganizationMemberResponse member = memberService.updateMemberRole(memberId, request);
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", member));
    }
    
    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable String organizationId,
            @PathVariable String memberId) {
        memberService.removeMember(memberId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
}

