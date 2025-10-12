package com.Flyway.Flyway.controller;

import com.Flyway.Flyway.dto.generated.AddOrganizationMemberRequest;
import com.Flyway.Flyway.dto.generated.UpdateMemberRoleRequest;
import com.Flyway.Flyway.dto.generated.OrganizationMemberResponse;
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
    public ResponseEntity<OrganizationMemberResponse> getMemberById(
            @PathVariable String memberId) {
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
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        OrganizationMemberResponse member = memberService.updateMemberRole(memberId, request);
        return ResponseEntity.ok(member);
    }
    
    @DeleteMapping("/{memberId}")
    @RequirePermission("member.remove")
    public ResponseEntity<Void> removeMember(
            @PathVariable String memberId) {
        memberService.removeMember(memberId);
        return ResponseEntity.ok().build();
    }
}

