# Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Step 1: Start the Infrastructure
```bash
docker-compose up -d
```

This starts MySQL, phpMyAdmin, and MailHog.

### Step 2: Build and Run the Application
```bash
mvn clean install
mvn spring-boot:run
```

The application will:
- Run Flyway migrations to create all database tables
- Generate JOOQ classes
- Start the server on http://localhost:8080

### Step 3: Test the API

#### Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

Save the `accessToken` from the response for authenticated requests.

#### Get Current User
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### Create an Organization
```bash
curl -X POST http://localhost:8080/api/organizations \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My First Organization"
  }'
```

## 📊 Access Management Tools

- **phpMyAdmin**: http://localhost:8081
  - Server: `mysql`
  - Username: `root`
  - Password: `root`

- **MailHog**: http://localhost:8025
  - View all captured emails

## 🔑 Default Data

After migrations, the following default data is available:

### User Statuses
- `active` - Active users
- `suspended` - Suspended users
- `deleted` - Deleted users

### Invitation Statuses
- `pending` - Pending invitations
- `accepted` - Accepted invitations
- `rejected` - Rejected invitations
- `expired` - Expired invitations

### Permissions
- `manage_users` - Can add, edit, and remove users
- `manage_roles` - Can create and manage roles
- `view_members` - Can view organization members
- `deploy_updates` - Can deploy application updates
- `view_deployments` - Can view deployment history
- `manage_billing` - Can manage billing and subscriptions
- `view_billing` - Can view billing information

## 📝 Complete API Flow Example

### 1. User Registration & Organization Creation

```bash
# Register user
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Admin",
    "email": "alice@example.com",
    "password": "SecurePass123"
  }')

# Extract access token
ACCESS_TOKEN=$(echo $REGISTER_RESPONSE | jq -r '.data.accessToken')

# Create organization
ORG_RESPONSE=$(curl -s -X POST http://localhost:8080/api/organizations \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Corporation"
  }')

ORG_ID=$(echo $ORG_RESPONSE | jq -r '.data.id')
```

### 2. Role & Permission Management

```bash
# Get all permissions
curl -X GET http://localhost:8080/api/permissions \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Get roles for organization
curl -X GET "http://localhost:8080/api/roles/organization/$ORG_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Create custom role with permissions
curl -X POST http://localhost:8080/api/roles \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Developer\",
    \"organizationId\": \"$ORG_ID\",
    \"permissionIds\": [\"PERMISSION_ID_1\", \"PERMISSION_ID_2\"]
  }"
```

### 3. Invite Users to Organization

```bash
# Create invitation
INVITE_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/invitations/organization/$ORG_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bob@example.com",
    "roleId": "ROLE_ID"
  }')

INVITE_TOKEN=$(echo $INVITE_RESPONSE | jq -r '.data.token')

# Register the invited user
BOB_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Bob",
    "lastName": "Developer",
    "email": "bob@example.com",
    "password": "SecurePass123"
  }')

BOB_TOKEN=$(echo $BOB_RESPONSE | jq -r '.data.accessToken')

# Accept invitation
curl -X POST "http://localhost:8080/api/invitations/token/$INVITE_TOKEN/respond" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "response": "accept"
  }'
```

### 4. View Organization Members

```bash
curl -X GET "http://localhost:8080/api/organizations/$ORG_ID/members" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## 🛠️ Common Operations

### Update User Profile
```bash
curl -X PUT http://localhost:8080/api/users/USER_ID \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith"
  }'
```

### Refresh Access Token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

### Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

## ⚠️ Important Notes

1. **JWT Secret**: Change the JWT secret in `application.properties` before deploying to production!

2. **Organization Owner**: When you create an organization, you automatically become the owner with an "Owner" role (system role, immutable).

3. **Role Management**: 
   - System roles (like "Owner") cannot be modified or deleted
   - Custom roles can be created, updated, and deleted
   - Each role can have multiple permissions

4. **Invitations**: 
   - Invitations expire after 7 days
   - The invited email must match the user's registration email
   - Users can only accept invitations for their own email

5. **JOOQ Code Generation**: 
   - Run `mvn clean install` whenever you modify database schema
   - JOOQ classes are generated in `target/generated-sources/jooq/`

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Check what's using port 8080
lsof -i :8080

# Kill the process
kill -9 PID
```

### MySQL Connection Issues
```bash
# Check if MySQL is running
docker ps

# View MySQL logs
docker logs flyway_mysql

# Restart MySQL
docker-compose restart mysql
```

### JOOQ Generation Fails
```bash
# Ensure MySQL is running and accessible
mysql -h localhost -u root -proot -e "SHOW DATABASES;"

# Clean and rebuild
mvn clean install -U
```

## 📚 Next Steps

1. Read the full [README.md](README.md) for complete documentation
2. Check the [ERD documentation](erd/1.%20auth%20module.md) for database schema
3. Explore the API endpoints using tools like Postman or Insomnia
4. Customize the application for your specific use case

## 💡 Tips

- Use phpMyAdmin to visually explore the database structure
- MailHog captures all emails sent by the application (useful for testing)
- All API responses follow a consistent format with `success`, `message`, and `data` fields
- Use proper HTTP status codes to handle different scenarios in your client application

