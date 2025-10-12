package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.AddOrganizationMemberRequest;
import com.Flyway.server.dto.generated.UpdateMemberRoleRequest;
import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.OrganizationMemberService;
import com.Flyway.server.dto.generated.OrganizationMemberResponse;

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
    public ResponseEntity<OrganizationMemberResponse> getMemberById(
            @PathVariable String memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Verify member belongs to user's organization
        memberService.verifyMemberOwnership(memberId, userDetails.getOrganizationId());
        OrganizationMemberResponse member = memberService.getMemberById(memberId);
        return ResponseEntity.ok(member);
    }
    
    @GetMapping
    @RequirePermission("member.view")
    public ResponseEntity<List<OrganizationMemberResponse>> getMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrganizationMemberResponse> members = memberService.getMembersByOrganizationId(userDetails.getOrganizationId());
        return ResponseEntity.ok(members);
    }
    
    @PostMapping
    @RequirePermission("member.add")
    public ResponseEntity<OrganizationMemberResponse> addMember(
            @Valid @RequestBody AddOrganizationMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrganizationMemberResponse member = memberService.addMember(userDetails.getOrganizationId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }
    
    @PutMapping("/{memberId}")
    @RequirePermission("member.update_role")
    public ResponseEntity<OrganizationMemberResponse> updateMemberRole(
            @PathVariable String memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Verify member belongs to user's organization
        memberService.verifyMemberOwnership(memberId, userDetails.getOrganizationId());
        OrganizationMemberResponse member = memberService.updateMemberRole(memberId, request);
        return ResponseEntity.ok(member);
    }
    
    @DeleteMapping("/{memberId}")
    @RequirePermission("member.remove")
    public ResponseEntity<Void> removeMember(
            @PathVariable String memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Verify member belongs to user's organization
        memberService.verifyMemberOwnership(memberId, userDetails.getOrganizationId());
        memberService.removeMember(memberId);
        return ResponseEntity.ok().build();
    }
}

