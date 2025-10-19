package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.UpdateUserRequest;
import com.Flyway.server.dto.generated.UserResponse;
import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    
}

