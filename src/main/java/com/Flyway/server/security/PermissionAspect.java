package com.Flyway.server.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.Flyway.server.exception.ForbiddenException;
import com.Flyway.server.exception.UnauthorizedException;
import com.Flyway.server.repository.OrganizationRepository;
import com.Flyway.server.service.PermissionService;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    
    private final PermissionService permissionService;
    private final OrganizationRepository organizationRepository;
    
    @Before("@annotation(com.Flyway.server.security.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();
        String organizationId = userDetails.getOrganizationId();
        
        // Get the method and annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        
        String requiredPermission = annotation.value();
        
        log.debug("Checking permission '{}' for user '{}' in organization '{}'", 
                  requiredPermission, userId, organizationId);
        
        if (organizationId == null) {
            throw new ForbiddenException("User is not a member of any organization");
        }
        
        // Check if user is the organization owner - owners have all permissions
        boolean isOwner = organizationRepository.isUserOrganizationOwner(userId, organizationId);
        
        if (isOwner) {
            log.debug("User '{}' is the owner of organization '{}', granting permission '{}'", 
                      userId, organizationId, requiredPermission);
            return; // Organization owners have all permissions
        }
        
        // Check if user has the required permission
        boolean hasPermission = permissionService.userHasPermission(userId, organizationId, requiredPermission);
        
        if (!hasPermission) {
            log.warn("User '{}' does not have permission '{}' in organization '{}'", 
                     userId, requiredPermission, organizationId);
            throw new ForbiddenException(
                "You do not have permission to perform this action. Required permission: " + requiredPermission
            );
        }
        
        log.debug("User '{}' has permission '{}' in organization '{}'", 
                  userId, requiredPermission, organizationId);
    }
}
