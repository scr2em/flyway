package com.Flyway.Flyway.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require specific permissions for accessing a controller method.
 * The permission check is performed within the context of the authenticated user's organization.
 * 
 * Usage:
 * @RequirePermission("organization.update")
 * 
 * The aspect will automatically extract the organizationId from the authenticated user's details.
 * Users can only be in one organization, so the permission check is performed against that organization.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * The permission code required to access this endpoint
     */
    String value();
}

