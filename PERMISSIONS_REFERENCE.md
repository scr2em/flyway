# Bitwise Permissions Reference

## Overview

The permission system has been refactored to use Discord-style bitwise operations. Permissions are now stored as a single `BIGINT` value in the database, where each permission is represented by a bit flag.

## How It Works

### Permission Enum

All permissions are defined in `Permission.java` enum with unique bit values:

```java
// Example permissions:
ORGANIZATION_UPDATE = 1L << 0  = 1
MEMBER_VIEW         = 1L << 3  = 8
MEMBER_ADD          = 1L << 4  = 16
ROLE_CREATE         = 1L << 8  = 256
// ... etc
```

### Available Permissions

| Permission Code | Bit Position | Bit Value | Description |
|----------------|--------------|-----------|-------------|
| `organization.update` | 0 | 1 | Update organization information |
| `member.view` | 3 | 8 | View organization members |
| `member.add` | 4 | 16 | Add new members |
| `member.remove` | 5 | 32 | Remove members |
| `member.update_role` | 6 | 64 | Update member roles |
| `role.view` | 7 | 128 | View roles |
| `role.create` | 8 | 256 | Create new roles |
| `role.update` | 9 | 512 | Update existing roles |
| `role.delete` | 10 | 1024 | Delete roles |
| `role.assign_permissions` | 11 | 2048 | Assign permissions to roles |
| `permission.view` | 12 | 4096 | View all permissions |
| `invitation.view` | 13 | 8192 | View invitations |
| `invitation.create` | 14 | 16384 | Create invitations |
| `invitation.cancel` | 15 | 32768 | Cancel invitations |
| `user.view` | 16 | 65536 | View user information |
| `user.update` | 17 | 131072 | Update user information |
| `user.delete` | 18 | 262144 | Delete users |
| `deployment.create` | 19 | 524288 | Deploy updates |
| `deployment.view` | 20 | 1048576 | View deployment history |
| `deployment.rollback` | 21 | 2097152 | Rollback deployments |
| `billing.view` | 22 | 4194304 | View billing information |
| `billing.manage` | 23 | 8388608 | Manage billing |

## Frontend Usage

### Creating a Role

When creating a role, send the permissions as a string containing the bitwise combined value:

```javascript
// Example: Give a role permission to view and add members
const permissions = {
  memberView: 8,      // 1 << 3
  memberAdd: 16       // 1 << 4
};

const permissionsValue = permissions.memberView | permissions.memberAdd;
// permissionsValue = 24

const roleData = {
  name: "Member Manager",
  description: "Can view and add members",
  permissions: String(permissionsValue) // Send as string: "24"
};

// POST /api/roles
```

### Updating a Role

Same as creating - send the full permissions value as a string:

```javascript
const updateData = {
  name: "Updated Role Name",
  description: "Updated description",
  permissions: "1048576" // Example: deployment.view only
};

// PUT /api/roles/{id}
```

### Reading Role Permissions

The API returns both:
1. `permissionsValue` - The bitwise value as a string
2. `permissions` - Array of permission objects for display

```javascript
// GET /api/roles/{id}
// Response:
{
  "id": "role-123",
  "name": "Admin",
  "permissionsValue": "16777215",  // Use this for editing
  "permissions": [                  // Use this for display
    {
      "code": "organization.update",
      "name": "Update Organization",
      "description": "Can update organization information",
      "resource": "organization",
      "action": "update"
    },
    // ... more permissions
  ]
}
```

### Bitwise Operations in JavaScript

```javascript
// Check if a permission is set
function hasPermission(permissions, permissionBit) {
  return (BigInt(permissions) & BigInt(permissionBit)) === BigInt(permissionBit);
}

// Add a permission
function addPermission(permissions, permissionBit) {
  return String(BigInt(permissions) | BigInt(permissionBit));
}

// Remove a permission
function removePermission(permissions, permissionBit) {
  return String(BigInt(permissions) & ~BigInt(permissionBit));
}

// Toggle a permission
function togglePermission(permissions, permissionBit) {
  return String(BigInt(permissions) ^ BigInt(permissionBit));
}

// Example usage:
let rolePermissions = "0";
rolePermissions = addPermission(rolePermissions, 8);    // Add member.view
rolePermissions = addPermission(rolePermissions, 16);   // Add member.add
// rolePermissions is now "24"

const canViewMembers = hasPermission(rolePermissions, 8); // true
const canDeleteRoles = hasPermission(rolePermissions, 1024); // false
```

### Complete Permission Constants for Frontend

```javascript
export const PERMISSIONS = {
  ORGANIZATION_UPDATE: { code: 'organization.update', bit: 1n },
  
  MEMBER_VIEW: { code: 'member.view', bit: 8n },
  MEMBER_ADD: { code: 'member.add', bit: 16n },
  MEMBER_REMOVE: { code: 'member.remove', bit: 32n },
  MEMBER_UPDATE_ROLE: { code: 'member.update_role', bit: 64n },
  
  ROLE_VIEW: { code: 'role.view', bit: 128n },
  ROLE_CREATE: { code: 'role.create', bit: 256n },
  ROLE_UPDATE: { code: 'role.update', bit: 512n },
  ROLE_DELETE: { code: 'role.delete', bit: 1024n },
  ROLE_ASSIGN_PERMISSIONS: { code: 'role.assign_permissions', bit: 2048n },
  
  PERMISSION_VIEW: { code: 'permission.view', bit: 4096n },
  
  INVITATION_VIEW: { code: 'invitation.view', bit: 8192n },
  INVITATION_CREATE: { code: 'invitation.create', bit: 16384n },
  INVITATION_CANCEL: { code: 'invitation.cancel', bit: 32768n },
  
  USER_VIEW: { code: 'user.view', bit: 65536n },
  USER_UPDATE: { code: 'user.update', bit: 131072n },
  USER_DELETE: { code: 'user.delete', bit: 262144n },
  
  DEPLOYMENT_CREATE: { code: 'deployment.create', bit: 524288n },
  DEPLOYMENT_VIEW: { code: 'deployment.view', bit: 1048576n },
  DEPLOYMENT_ROLLBACK: { code: 'deployment.rollback', bit: 2097152n },
  
  BILLING_VIEW: { code: 'billing.view', bit: 4194304n },
  BILLING_MANAGE: { code: 'billing.manage', bit: 8388608n }
};
```

## Benefits

1. **Performance**: Single integer comparison instead of database joins
2. **Scalability**: Can support up to 64 permissions (using `BIGINT`)
3. **Simplicity**: One field instead of many-to-many relationship
4. **Flexibility**: Easy to check, add, or remove permissions using bitwise operations

## Backend Changes

- **Database**: Added `permissions` (BIGINT) and `description` (TEXT) columns to `roles` table
- **Removed**: `permissions` and `role_permissions` tables
- **New Files**: 
  - `Permission.java` enum
  - `PermissionUtil.java` utility class
- **Updated**: All services and repositories to use bitwise operations

