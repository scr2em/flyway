package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.UpdateUserRequest;
import com.Flyway.server.dto.generated.UserResponse;
import com.Flyway.server.exception.ForbiddenException;
import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse user = userService.getUserById(userDetails.getId());
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Users can only view themselves unless they have user.view permission
        if (!id.equals(userDetails.getId())) {
            // This will throw ForbiddenException if user doesn't have permission
            userService.checkUserPermission(userDetails.getId(), userDetails.getOrganizationId(), "user.view");
        }
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping
    @RequirePermission("user.view")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Only return users from the current user's organization
        List<UserResponse> users = userService.getUsersByOrganizationId(userDetails.getOrganizationId());
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Users can only update themselves unless they have user.update permission
        if (!id.equals(userDetails.getId())) {
            userService.checkUserPermission(userDetails.getId(), userDetails.getOrganizationId(), "user.update");
        }
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/{id}")
    @RequirePermission("user.delete")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Prevent self-deletion
        if (id.equals(userDetails.getId())) {
            throw new ForbiddenException("You cannot delete your own account");
        }
        // Verify target user belongs to same organization
        userService.verifyUserInOrganization(id, userDetails.getOrganizationId());
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/verify-email")
    public ResponseEntity<UserResponse> verifyEmail(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Users can only verify their own email
        if (!id.equals(userDetails.getId())) {
            throw new ForbiddenException("You can only verify your own email address");
        }
        UserResponse user = userService.verifyEmail(id);
        return ResponseEntity.ok(user);
    }
}

