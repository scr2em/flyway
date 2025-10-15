package com.Flyway.server.repository;

import com.Flyway.server.jooq.tables.records.ChannelsRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.Flyway.server.jooq.tables.Channels.CHANNELS;

@Repository
@RequiredArgsConstructor
public class ChannelRepository {
    
    private final DSLContext dsl;
    
    /**
     * Find a channel by ID
     */
    public Optional<ChannelsRecord> findById(String id) {
        return dsl.selectFrom(CHANNELS)
                .where(CHANNELS.ID.eq(id))
                .fetchOptional();
    }
    
    /**
     * Find a channel by ID and organization
     */
    public Optional<ChannelsRecord> findByIdAndOrganizationId(String id, String organizationId) {
        return dsl.selectFrom(CHANNELS)
                .where(CHANNELS.ID.eq(id)
                        .and(CHANNELS.ORGANIZATION_ID.eq(organizationId)))
                .fetchOptional();
    }
    
    /**
     * Find all channels for an organization
     */
    public List<ChannelsRecord> findByOrganizationId(String organizationId) {
        return dsl.selectFrom(CHANNELS)
                .where(CHANNELS.ORGANIZATION_ID.eq(organizationId))
                .orderBy(CHANNELS.CREATED_AT.desc())
                .fetch();
    }
    
    /**
     * Find channels by organization with pagination and sorting
     */
    public List<ChannelsRecord> findByOrganizationIdPaginated(
            String organizationId,
            int limit,
            int offset,
            String sortDirection) {
        
        SortField<?> sortField = sortDirection.equalsIgnoreCase("asc") 
                ? CHANNELS.CREATED_AT.asc() 
                : CHANNELS.CREATED_AT.desc();
        
        return dsl.selectFrom(CHANNELS)
                .where(CHANNELS.ORGANIZATION_ID.eq(organizationId))
                .orderBy(sortField)
                .limit(limit)
                .offset(offset)
                .fetch();
    }
    
    /**
     * Count channels by organization
     */
    public int countByOrganizationId(String organizationId) {
        return dsl.fetchCount(
                dsl.selectFrom(CHANNELS)
                        .where(CHANNELS.ORGANIZATION_ID.eq(organizationId))
        );
    }
    
    /**
     * Check if a channel with the given name exists in the organization
     */
    public boolean existsByNameAndOrganizationId(String name, String organizationId) {
        return dsl.fetchExists(
                dsl.selectFrom(CHANNELS)
                        .where(CHANNELS.NAME.eq(name)
                                .and(CHANNELS.ORGANIZATION_ID.eq(organizationId)))
        );
    }
    
    /**
     * Check if a channel with the given name exists in the organization, excluding a specific channel ID
     */
    public boolean existsByNameAndOrganizationIdExcludingId(String name, String organizationId, String excludeId) {
        return dsl.fetchExists(
                dsl.selectFrom(CHANNELS)
                        .where(CHANNELS.NAME.eq(name)
                                .and(CHANNELS.ORGANIZATION_ID.eq(organizationId))
                                .and(CHANNELS.ID.ne(excludeId)))
        );
    }
    
    /**
     * Create a new channel
     */
    public ChannelsRecord create(
            String id,
            String name,
            String description,
            String organizationId) {
        
        LocalDateTime now = LocalDateTime.now();
        
        ChannelsRecord record = dsl.newRecord(CHANNELS);
        record.setId(id);
        record.setName(name);
        record.setDescription(description);
        record.setOrganizationId(organizationId);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.store();
        
        return record;
    }
    
    /**
     * Update a channel
     */
    public void update(String id, String name, String description) {
        dsl.update(CHANNELS)
                .set(CHANNELS.NAME, name)
                .set(CHANNELS.DESCRIPTION, description)
                .set(CHANNELS.UPDATED_AT, LocalDateTime.now())
                .where(CHANNELS.ID.eq(id))
                .execute();
    }
    
    /**
     * Delete a channel by ID
     */
    public int delete(String id) {
        return dsl.deleteFrom(CHANNELS)
                .where(CHANNELS.ID.eq(id))
                .execute();
    }
    
    /**
     * Check if a channel exists by ID
     */
    public boolean existsById(String id) {
        return dsl.fetchExists(
                dsl.selectFrom(CHANNELS)
                        .where(CHANNELS.ID.eq(id))
        );
    }
}

