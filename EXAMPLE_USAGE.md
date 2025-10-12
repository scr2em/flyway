# Example Usage of Generated DTOs

This document shows practical examples of using auto-generated DTOs from OpenAPI spec.

## Table of Contents
1. [Basic Controller Example](#basic-controller-example)
2. [Service Layer Example](#service-layer-example)
3. [Validation Example](#validation-example)
4. [Error Handling](#error-handling)
5. [Converting Between Generated and JOOQ Records](#converting-between-generated-and-jooq-records)

---

## Basic Controller Example

### Generated DTOs from OpenAPI

When you define this in `openapi.yaml`:

```yaml
components:
  schemas:
    CreateUserRequest:
      type: object
      required: [email, firstName, lastName]
      properties:
        email:
          type: string
          format: email
        firstName:
          type: string
          minLength: 1
          maxLength: 100
        lastName:
          type: string
          minLength: 1
          maxLength: 100
    
    UserResponse:
      type: object
      required: [id, email, firstName, lastName, createdAt]
      properties:
        id:
          type: integer
          format: int64
        email:
          type: string
          format: email
        firstName:
          type: string
        lastName:
          type: string
        createdAt:
          type: string
          format: date-time
```

### Using in Controller

```java
package com.Flyway.server.controller;

import com.Flyway.server.dto.generated.*;
import com.Flyway.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * Create a new user
     * ✅ @Valid automatically validates based on OpenAPI constraints
     * ✅ CreateUserRequest has @NotNull, @Email, @Size annotations generated
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get current user
     * ✅ Returns generated UserResponse
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication auth) {
        UserResponse response = userService.getCurrentUser(auth);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update current user
     * ✅ Uses generated UpdateUserRequest
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication auth) {
        
        UserResponse response = userService.updateCurrentUser(request, auth);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all users (paginated)
     * ✅ Returns List of generated UserResponse
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<UserResponse> users = userService.getUsers(page, size);
        return ResponseEntity.ok(users);
    }
}
```

---

## Service Layer Example

```java
package com.Flyway.server.service;

import com.Flyway.server.dto.generated.*;
import com.Flyway.server.jooq.tables.records.UsersRecord;
import com.Flyway.server.repository.UserRepository;
import com.Flyway.server.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Create a new user from generated request DTO
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Convert request DTO to database record
        UsersRecord record = new UsersRecord();
        record.setEmail(request.getEmail());
        record.setFirstName(request.getFirstName());
        record.setLastName(request.getLastName());
        record.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        record.setStatusId(1L); // Active status
        record.setCreatedAt(OffsetDateTime.now());
        
        // 2. Save to database using JOOQ repository
        UsersRecord saved = userRepository.create(record);
        
        // 3. Convert database record to response DTO
        return toUserResponse(saved);
    }
    
    /**
     * Get current user
     */
    public UserResponse getCurrentUser(Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        UsersRecord record = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return toUserResponse(record);
    }
    
    /**
     * Update user
     */
    @Transactional
    public UserResponse updateCurrentUser(UpdateUserRequest request, Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        UsersRecord record = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update only provided fields (optional fields in DTO)
        if (request.getFirstName() != null) {
            record.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            record.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            record.setEmail(request.getEmail());
        }
        
        record.setUpdatedAt(OffsetDateTime.now());
        userRepository.update(record);
        
        return toUserResponse(record);
    }
    
    /**
     * Get all users
     */
    public List<UserResponse> getUsers(int page, int size) {
        List<UsersRecord> records = userRepository.findAll(page, size);
        
        return records.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }
    
    // ========================================
    // Helper Methods: Convert between DTOs and JOOQ Records
    // ========================================
    
    /**
     * Convert JOOQ UsersRecord to generated UserResponse DTO
     */
    private UserResponse toUserResponse(UsersRecord record) {
        return UserResponse.builder()
                .id(record.getId())
                .email(record.getEmail())
                .firstName(record.getFirstName())
                .lastName(record.getLastName())
                .organizationId(record.getOrganizationId())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
    
    private Long getUserIdFromAuth(Authentication auth) {
        // Extract user ID from authentication
        return ((CustomUserDetails) auth.getPrincipal()).getUserId();
    }
}
```

---

## Validation Example

### OpenAPI Constraints → Bean Validation

When you define constraints in OpenAPI:

```yaml
CreateProductRequest:
  type: object
  required: [name, price]
  properties:
    name:
      type: string
      minLength: 3
      maxLength: 200
    price:
      type: number
      format: float
      minimum: 0.01
      maximum: 999999.99
    description:
      type: string
      maxLength: 1000
    tags:
      type: array
      items:
        type: string
      minItems: 1
      maxItems: 10
```

Generated Java DTO will have:

```java
public class CreateProductRequest {
    
    @NotNull
    @Size(min = 3, max = 200)
    private String name;
    
    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    @DecimalMax(value = "999999.99", inclusive = true)
    private BigDecimal price;
    
    @Size(max = 1000)
    private String description;
    
    @Size(min = 1, max = 10)
    private List<String> tags;
}
```

### Using Validation in Controller

```java
@PostMapping("/products")
public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody CreateProductRequest request) {
    
    // If validation fails, Spring automatically returns 400 Bad Request
    // with error details before this code executes
    
    ProductResponse response = productService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Validation Error Response

When validation fails, your `GlobalExceptionHandler` catches it:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        ErrorResponse error = ErrorResponse.builder()
                .error("Validation Error")
                .message("Invalid request data")
                .timestamp(OffsetDateTime.now())
                .build();
        
        return ResponseEntity.badRequest().body(error);
    }
}
```

Example error response:
```json
{
  "error": "Validation Error",
  "message": "Invalid request data",
  "details": "price: must be greater than or equal to 0.01",
  "timestamp": "2025-10-12T10:30:00Z"
}
```

---

## Error Handling

### Define Error Response in OpenAPI

```yaml
components:
  schemas:
    ErrorResponse:
      type: object
      required: [error, message, timestamp]
      properties:
        error:
          type: string
        message:
          type: string
        details:
          type: string
        timestamp:
          type: string
          format: date-time
  
  responses:
    NotFoundError:
      description: Resource not found
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    
    ValidationError:
      description: Validation error
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
```

### Use Generated ErrorResponse

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex) {
        
        // Use generated ErrorResponse DTO
        ErrorResponse error = ErrorResponse.builder()
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex) {
        
        ErrorResponse error = ErrorResponse.builder()
                .error("Conflict")
                .message(ex.getMessage())
                .details("Resource already exists")
                .timestamp(OffsetDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
```

---

## Converting Between Generated and JOOQ Records

### Utility Class for Conversions

Create a mapper utility:

```java
package com.Flyway.server.util;

import com.Flyway.server.dto.generated.*;
import com.Flyway.server.jooq.tables.records.*;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    
    // ========================================
    // User Conversions
    // ========================================
    
    public UserResponse toUserResponse(UsersRecord record) {
        if (record == null) return null;
        
        return UserResponse.builder()
                .id(record.getId())
                .email(record.getEmail())
                .firstName(record.getFirstName())
                .lastName(record.getLastName())
                .organizationId(record.getOrganizationId())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
    
    public UsersRecord toUsersRecord(CreateUserRequest request) {
        UsersRecord record = new UsersRecord();
        record.setEmail(request.getEmail());
        record.setFirstName(request.getFirstName());
        record.setLastName(request.getLastName());
        return record;
    }
    
    // ========================================
    // Organization Conversions
    // ========================================
    
    public OrganizationResponse toOrganizationResponse(OrganizationsRecord record) {
        if (record == null) return null;
        
        return OrganizationResponse.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
    
    public OrganizationsRecord toOrganizationsRecord(CreateOrganizationRequest request) {
        OrganizationsRecord record = new OrganizationsRecord();
        record.setName(request.getName());
        record.setDescription(request.getDescription());
        return record;
    }
    
    // ========================================
    // Role Conversions
    // ========================================
    
    public RoleResponse toRoleResponse(RolesRecord record) {
        if (record == null) return null;
        
        return RoleResponse.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .organizationId(record.getOrganizationId())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
```

### Using the Mapper in Service

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;  // ✅ Inject the mapper
    
    public UserResponse getCurrentUser(Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        UsersRecord record = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // ✅ Use mapper instead of manual conversion
        return dtoMapper.toUserResponse(record);
    }
    
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // ✅ Use mapper to convert request to record
        UsersRecord record = dtoMapper.toUsersRecord(request);
        record.setStatusId(1L);
        record.setCreatedAt(OffsetDateTime.now());
        
        UsersRecord saved = userRepository.create(record);
        
        // ✅ Use mapper to convert record to response
        return dtoMapper.toUserResponse(saved);
    }
}
```

---

## Best Practices

### ✅ DO:

1. **Use `@Valid` for automatic validation**
   ```java
   public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request)
   ```

2. **Create a mapper utility for conversions**
   ```java
   @Component
   public class DtoMapper {
       public UserResponse toUserResponse(UsersRecord record) { ... }
   }
   ```

3. **Use Lombok builders for creating DTOs**
   ```java
   return UserResponse.builder()
       .id(record.getId())
       .email(record.getEmail())
       .build();
   ```

4. **Handle optional fields properly**
   ```java
   if (request.getFirstName() != null) {
       record.setFirstName(request.getFirstName());
   }
   ```

### ❌ DON'T:

1. **Don't edit generated DTOs** - They'll be overwritten on next build

2. **Don't skip `@Valid` annotation** - You lose automatic validation

3. **Don't expose JOOQ records directly** - Always convert to DTOs
   ```java
   // ❌ BAD
   public UsersRecord getUser() { ... }
   
   // ✅ GOOD
   public UserResponse getUser() { ... }
   ```

4. **Don't create manual DTOs for API contracts** - Define in OpenAPI instead

---

## Summary

✅ Define schemas in `openapi.yaml`  
✅ Generate DTOs with `mvn generate-sources`  
✅ Use `@Valid` for automatic validation  
✅ Create mapper utilities for conversions  
✅ Never edit generated code  
✅ Keep API contracts in sync with frontend  

**For more details, see:**
- [OPENAPI_WORKFLOW.md](./OPENAPI_WORKFLOW.md) - Complete workflow guide
- [swagger-ts.md](./swagger-ts.md) - OpenAPI best practices

