package com.Flyway.server.service;

import com.Flyway.server.dto.generated.ChannelResponse;
import com.Flyway.server.dto.generated.PaginatedChannelResponse;
import com.Flyway.server.exception.BadRequestException;
import com.Flyway.server.exception.ConflictException;
import com.Flyway.server.exception.ForbiddenException;
import com.Flyway.server.exception.ResourceNotFoundException;
import com.Flyway.server.jooq.tables.records.ChannelsRecord;
import com.Flyway.server.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {
    
    private final ChannelRepository channelRepository;
    
    /**
     * Get channels with pagination and sorting
     */
    public PaginatedChannelResponse getChannelsPaginated(
            String organizationId,
            int page,
            int size,
            String sort,
            String authenticatedUserOrgId) {
        
        // Validate that the user has access to this organization
        if (!organizationId.equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
        // Validate sort parameter
        if (!sort.equalsIgnoreCase("asc") && !sort.equalsIgnoreCase("desc")) {
            throw new BadRequestException("Sort parameter must be either 'asc' or 'desc'");
        }
        
        int offset = page * size;
        
        List<ChannelsRecord> channels = channelRepository.findByOrganizationIdPaginated(
                organizationId, size, offset, sort);
        int totalCount = channelRepository.countByOrganizationId(organizationId);
        
        List<ChannelResponse> channelResponses = channels.stream()
                .map(this::mapToChannelResponse)
                .collect(Collectors.toList());
        
        return new PaginatedChannelResponse()
                .data(channelResponses)
                .page(page)
                .size(size)
                .totalElements(totalCount)
                .totalPages((int) Math.ceil((double) totalCount / size));
    }
    
    /**
     * Create a new channel
     */
    @Transactional
    public ChannelResponse createChannel(
            String name,
            String description,
            String organizationId,
            String authenticatedUserOrgId) {
        
        // Validate that the user has access to this organization
        if (!organizationId.equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
        // Validate name is not empty
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Channel name cannot be empty");
        }
        
        // Check if a channel with this name already exists in the organization
        if (channelRepository.existsByNameAndOrganizationId(name, organizationId)) {
            throw new ConflictException("A channel with this name already exists in this organization");
        }
        
        // Create the channel record
        String id = UUID.randomUUID().toString();
        ChannelsRecord record = channelRepository.create(
                id,
                name,
                description,
                organizationId
        );
        
        return mapToChannelResponse(record);
    }
    
    /**
     * Update a channel
     */
    @Transactional
    public ChannelResponse updateChannel(
            String channelId,
            String name,
            String description,
            String organizationId,
            String authenticatedUserOrgId) {
        
        // Validate that the user has access to this organization
        if (!organizationId.equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
        // Find the channel
        ChannelsRecord channel = channelRepository.findByIdAndOrganizationId(channelId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));
        
        // If name is being updated, validate it
        if (name != null && !name.trim().isEmpty()) {
            // Check if another channel with this name already exists in the organization
            if (!name.equals(channel.getName()) && 
                channelRepository.existsByNameAndOrganizationIdExcludingId(name, organizationId, channelId)) {
                throw new ConflictException("A channel with this name already exists in this organization");
            }
        } else if (name != null) {
            throw new BadRequestException("Channel name cannot be empty");
        }
        
        // Use existing values if not provided
        String updatedName = (name != null && !name.trim().isEmpty()) ? name : channel.getName();
        String updatedDescription = description != null ? description : channel.getDescription();
        
        // Update the channel
        channelRepository.update(channelId, updatedName, updatedDescription);
        
        // Fetch and return the updated channel
        ChannelsRecord updatedChannel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));
        
        return mapToChannelResponse(updatedChannel);
    }
    
    /**
     * Delete a channel
     */
    @Transactional
    public void deleteChannel(String channelId, String organizationId, String authenticatedUserOrgId) {
        
        // Validate that the user has access to this organization
        if (!organizationId.equals(authenticatedUserOrgId)) {
            throw new ForbiddenException("You do not have access to this organization");
        }
        
        // Verify the channel exists and belongs to the organization
        channelRepository.findByIdAndOrganizationId(channelId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));
        
        // Delete the channel
        if (channelRepository.delete(channelId) == 0) {
            throw new ResourceNotFoundException("Channel not found");
        }
    }
    
    /**
     * Map channel record to response
     */
    private ChannelResponse mapToChannelResponse(ChannelsRecord record) {
        return new ChannelResponse()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .organizationId(record.getOrganizationId())
                .createdAt(record.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(record.getUpdatedAt().atOffset(ZoneOffset.UTC));
    }
}

