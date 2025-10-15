# Role Permissions Quick Reference

This document provides the exact bitwise permission values for each predefined role. Use these values for testing, documentation, or manual role creation.

## Permission Bit Values

### Individual Permissions

| Permission | Bit Position | Bit Value |
|-----------|--------------|-----------|
| organization.update | 0 | 1 |
| member.view | 3 | 8 |
| member.add | 4 | 16 |
| member.remove | 5 | 32 |
| member.update_role | 6 | 64 |
| role.view | 7 | 128 |
| role.create | 8 | 256 |
| role.update | 9 | 512 |
| role.delete | 10 | 1024 |
| role.assign_permissions | 11 | 2048 |
| permission.view | 12 | 4096 |
| invitation.view | 13 | 8192 |
| invitation.create | 14 | 16384 |
| invitation.cancel | 15 | 32768 |
| user.view | 16 | 65536 |
| user.update | 17 | 131072 |
| user.delete | 18 | 262144 |
| deployment.create | 19 | 524288 |
| deployment.view | 20 | 1048576 |
| deployment.rollback | 21 | 2097152 |
| billing.view | 22 | 4194304 |
| billing.manage | 23 | 8388608 |

## Predefined Role Permission Values

### 1. Owner
**Permissions Value**: `16777215`
**Binary**: `111111111111111111111111` (all 24 bits set)
**Permissions**: ALL

### 2. Admin
**Permissions Value**: `8388607`
**Binary**: `011111111111111111111111` (all except bit 23)
**Calculation**: 
```
1 + 8 + 16 + 32 + 64 + 128 + 256 + 512 + 1024 + 2048 + 4096 + 
8192 + 16384 + 32768 + 65536 + 131072 + 262144 + 524288 + 1048576 + 
2097152 + 4194304 = 8388607
```
**Excluded**: billing.manage

### 3. Developer
**Permissions Value**: `3743880`
**Binary**: `001110010010010010001000`
**Calculation**:
```
member.view (8) + 
role.view (128) + 
invitation.view (8192) + 
user.view (65536) + 
deployment.create (524288) + 
deployment.view (1048576) + 
deployment.rollback (2097152) 
= 3743880
```

### 4. Analyst
**Permissions Value**: `5602912`
**Binary**: `010101011000000010001000`
**Calculation**:
```
member.view (8) + 
role.view (128) + 
permission.view (4096) + 
invitation.view (8192) + 
user.view (65536) + 
user.update (131072) + 
deployment.view (1048576) + 
billing.view (4194304) 
= 5602912
```

### 5. Support
**Permissions Value**: `1532912`
**Binary**: `000101110110000000001000`
**Calculation**:
```
member.view (8) + 
invitation.view (8192) + 
invitation.create (16384) + 
user.view (65536) + 
user.update (131072) + 
user.delete (262144) + 
deployment.view (1048576) 
= 1532912
```

## SQL Insert Statements

If you need to manually create these roles in an existing organization:

```sql
-- Replace {org_id} with your organization UUID

-- Admin Role
INSERT INTO roles (id, organization_id, name, description, permissions, is_system_role, is_immutable)
VALUES (
  UUID(), 
  '{org_id}', 
  'Admin', 
  'Manages team, roles, deployments, and can view billing. Cannot modify payment methods.',
  8388607,
  1,
  0
);

-- Developer Role
INSERT INTO roles (id, organization_id, name, description, permissions, is_system_role, is_immutable)
VALUES (
  UUID(), 
  '{org_id}', 
  'Developer', 
  'Can deploy app updates, view deployment history, and rollback changes. Read-only access to team.',
  3743880,
  1,
  0
);

-- Analyst Role
INSERT INTO roles (id, organization_id, name, description, permissions, is_system_role, is_immutable)
VALUES (
  UUID(), 
  '{org_id}', 
  'Analyst', 
  'Product managers and data analysts. Can view all data and manage user segments, but cannot deploy.',
  5602912,
  1,
  0
);

-- Support Role
INSERT INTO roles (id, organization_id, name, description, permissions, is_system_role, is_immutable)
VALUES (
  UUID(), 
  '{org_id}', 
  'Support', 
  'Customer support team. Can manage users, create invitations, and view deployment status.',
  1532912,
  1,
  0
);
```

## API Request Examples

### Creating a Custom Role with Developer Permissions

```bash
curl -X POST https://your-api.com/api/roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "Custom Developer",
    "description": "Developer with custom permissions",
    "permissions": "3743880"
  }'
```

### Updating a Role to Admin Permissions

```bash
curl -X PUT https://your-api.com/api/roles/{roleId} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "Updated Role",
    "description": "Now has admin permissions",
    "permissions": "8388607"
  }'
```

## Verification

To verify which permissions a role has, use bitwise AND operations:

```javascript
const rolePermissions = 3743880; // Developer
const hasDeployCreate = (rolePermissions & 524288) === 524288; // true
const hasBillingView = (rolePermissions & 4194304) === 4194304; // false
const hasMemberView = (rolePermissions & 8) === 8; // true
```

Or in Java:

```java
long rolePermissions = 3743880L; // Developer
boolean hasDeployCreate = PermissionUtil.hasPermission(rolePermissions, Permission.DEPLOYMENT_CREATE); // true
boolean hasBillingView = PermissionUtil.hasPermission(rolePermissions, Permission.BILLING_VIEW); // false
boolean hasMemberView = PermissionUtil.hasPermission(rolePermissions, Permission.MEMBER_VIEW); // true
```

## Testing Checklist

When testing predefined roles:

- [ ] Owner has all 24 permissions (value = 16777215)
- [ ] Admin has 21 permissions (missing billing.manage)
- [ ] Developer can create deployments but cannot manage billing
- [ ] Analyst can view billing but cannot manage it
- [ ] Support can delete users but cannot deploy
- [ ] Owner role is immutable (cannot be modified or deleted)
- [ ] Other predefined roles can be modified
- [ ] All predefined roles are marked as system roles

