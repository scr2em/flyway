package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.ChannelResponse;
import com.Flyway.server.dto.generated.CreateChannelRequest;
import com.Flyway.server.dto.generated.PaginatedChannelResponse;
import com.Flyway.server.dto.generated.UpdateChannelRequest;
import com.Flyway.server.security.CustomUserDetails;
import com.Flyway.server.security.RequirePermission;
import com.Flyway.server.service.ChannelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChannelController {
    
    private final ChannelService channelService;
    
    /**
     * Get all channels for an organization with pagination
     * 
     * @param orgId Organization ID
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort direction: "asc" or "desc" (default: "desc")
     */
    @GetMapping("/api/{orgId}/channels")
    @RequirePermission("channel.view")
    public ResponseEntity<PaginatedChannelResponse> getChannels(
            @PathVariable String orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        PaginatedChannelResponse response = channelService.getChannelsPaginated(
                orgId,
                page,
                size,
                sort,
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a new channel
     * 
     * @param orgId Organization ID
     * @param request Channel creation request
     */
    @PostMapping("/api/{orgId}/channels")
    @RequirePermission("channel.create")
    public ResponseEntity<ChannelResponse> createChannel(
            @PathVariable String orgId,
            @Valid @RequestBody CreateChannelRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        ChannelResponse response = channelService.createChannel(
                request.getName(),
                request.getDescription(),
                orgId,
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update a channel
     * 
     * @param orgId Organization ID
     * @param channelId Channel ID
     * @param request Channel update request
     */
    @PutMapping("/api/{orgId}/channels/{channelId}")
    @RequirePermission("channel.update")
    public ResponseEntity<ChannelResponse> updateChannel(
            @PathVariable String orgId,
            @PathVariable String channelId,
            @Valid @RequestBody UpdateChannelRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        ChannelResponse response = channelService.updateChannel(
                channelId,
                request.getName(),
                request.getDescription(),
                orgId,
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a channel
     * 
     * @param orgId Organization ID
     * @param channelId Channel ID
     */
    @DeleteMapping("/api/{orgId}/channels/{channelId}")
    @RequirePermission("channel.delete")
    public ResponseEntity<Void> deleteChannel(
            @PathVariable String orgId,
            @PathVariable String channelId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        channelService.deleteChannel(
                channelId,
                orgId,
                userDetails.getOrganizationId()
        );
        
        return ResponseEntity.noContent().build();
    }
}

