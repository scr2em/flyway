# Flyway - Capacitor Live Updates Platform

A Spring Boot application for managing live updates for Capacitor apps with complete authentication and authorization module.

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.0-SNAPSHOT**
- **MySQL 8.x**
- **JOOQ** for database access
- **JWT** for authentication
- **Flyway** for database migrations
- **Spring Security** for authorization
- **Lombok** for reducing boilerplate
- **Docker Compose** for infrastructure

## Architecture

### Database Tables (Auth Module)
1. **users** - User accounts
2. **user_statuses** - User status lookup (active, suspended, deleted)
3. **refresh_tokens** - JWT refresh tokens
4. **organizations** - Organizations/tenants
5. **roles** - Roles within organizations
6. **permissions** - System permissions
7. **role_permissions** - Many-to-many relationship
8. **organization_members** - Users belonging to organizations
9. **invitations** - Organization invitations
10. **invitation_statuses** - Invitation status lookup

### Project Structure
```
src/main/java/com/Flyway/Flyway/
├── config/              # Configuration classes (Security, JWT, Web)
├── controller/          # REST API endpoints
├── dto/                 # Data Transfer Objects
│   ├── request/        # Request DTOs
│   └── response/       # Response DTOs
├── exception/          # Custom exceptions and global error handler
├── repository/         # JOOQ repositories
├── security/           # Security components (filters, user details)
├── service/            # Business logic layer
└── util/               # Utility classes (JWT)
```

## Setup Instructions

### 1. Prerequisites
- Java 25
- Maven 3.8+
- Docker & Docker Compose (for MySQL, phpMyAdmin, MailHog)

### 2. Start Infrastructure
Start MySQL, phpMyAdmin, and MailHog using Docker Compose:
```bash
docker-compose up -d
```

This will start:
- MySQL on port 3306
- phpMyAdmin on port 8081
- MailHog on port 8025 (SMTP: 1025)

### 3. Configure Database
Update `src/main/resources/application.properties` if needed:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/flyway_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
```

### 4. Generate JOOQ Classes
After the database is running and tables are created by Flyway:
```bash
mvn clean install
```

This will:
- Run Flyway migrations to create all tables
- Generate JOOQ classes based on your database schema

### 5. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

### Authentication Endpoints

#### Register a New User
```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "uuid",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "emailVerified": false
    }
  },
  "timestamp": "2025-10-12T..."
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGc..."
}
```

#### Logout
```http
POST /api/auth/logout
Content-Type: application/json
Authorization: Bearer {accessToken}

{
  "refreshToken": "eyJhbGc..."
}
```

### User Endpoints

#### Get Current User
```http
GET /api/users/me
Authorization: Bearer {accessToken}
```

#### Get User by ID
```http
GET /api/users/{id}
Authorization: Bearer {accessToken}
```

#### Update User
```http
PUT /api/users/{id}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Smith"
}
```

#### Delete User
```http
DELETE /api/users/{id}
Authorization: Bearer {accessToken}
```

### Organization Endpoints

#### Create Organization
```http
POST /api/organizations
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "My Organization"
}
```

#### Get Organization by ID
```http
GET /api/organizations/{id}
Authorization: Bearer {accessToken}
```

#### Get My Organizations
```http
GET /api/organizations/my-organizations
Authorization: Bearer {accessToken}
```

#### Update Organization
```http
PUT /api/organizations/{id}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "Updated Organization Name"
}
```

#### Delete Organization
```http
DELETE /api/organizations/{id}
Authorization: Bearer {accessToken}
```

### Role Endpoints

#### Create Role
```http
POST /api/roles
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "Developer",
  "organizationId": "org-uuid",
  "permissionIds": ["perm-uuid-1", "perm-uuid-2"]
}
```

#### Get Roles by Organization
```http
GET /api/roles/organization/{organizationId}
Authorization: Bearer {accessToken}
```

#### Update Role
```http
PUT /api/roles/{id}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "Senior Developer",
  "permissionIds": ["perm-uuid-1", "perm-uuid-2", "perm-uuid-3"]
}
```

#### Delete Role
```http
DELETE /api/roles/{id}
Authorization: Bearer {accessToken}
```

### Permission Endpoints

#### Get All Permissions
```http
GET /api/permissions
Authorization: Bearer {accessToken}
```

#### Get Permissions by Category
```http
GET /api/permissions/category/{category}
Authorization: Bearer {accessToken}
```

### Organization Member Endpoints

#### Add Member to Organization
```http
POST /api/organizations/{organizationId}/members
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "userId": "user-uuid",
  "roleId": "role-uuid"
}
```

#### Get Organization Members
```http
GET /api/organizations/{organizationId}/members
Authorization: Bearer {accessToken}
```

#### Update Member Role
```http
PUT /api/organizations/{organizationId}/members/{memberId}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "roleId": "new-role-uuid"
}
```

#### Remove Member
```http
DELETE /api/organizations/{organizationId}/members/{memberId}
Authorization: Bearer {accessToken}
```

### Invitation Endpoints

#### Create Invitation
```http
POST /api/invitations/organization/{organizationId}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "email": "newuser@example.com",
  "roleId": "role-uuid"
}
```

#### Get My Invitations
```http
GET /api/invitations/my-invitations
Authorization: Bearer {accessToken}
```

#### Respond to Invitation
```http
POST /api/invitations/token/{token}/respond
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "response": "accept"
}
```
Note: `response` can be either "accept" or "reject"

#### Get Invitations by Organization
```http
GET /api/invitations/organization/{organizationId}
Authorization: Bearer {accessToken}
```

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`:
- `V1__create_user_statuses_table.sql`
- `V2__create_users_table.sql`
- `V3__create_refresh_tokens_table.sql`
- `V4__create_organizations_table.sql`
- `V5__create_roles_table.sql`
- `V6__create_organization_members_table.sql`
- `V7__create_permissions_table.sql`
- `V8__create_role_permissions_table.sql`
- `V9__create_invitation_statuses_table.sql`
- `V10__create_invitations_table.sql`

Migrations run automatically on application startup.

## Security

### JWT Configuration
- **Access Token Expiration**: 24 hours (86400000 ms)
- **Refresh Token Expiration**: 7 days (604800000 ms)
- **Secret Key**: Configure in `application.properties` (change for production!)

### Public Endpoints
- `/api/auth/**` - Authentication endpoints
- `/api/public/**` - Public endpoints
- `/actuator/**` - Actuator endpoints

All other endpoints require JWT authentication.

## Error Handling

The application uses a global exception handler that returns consistent error responses:

```json
{
  "success": false,
  "message": "Error message",
  "status": 404,
  "path": "/api/users/invalid-id",
  "timestamp": "2025-10-12T...",
  "errors": {
    "fieldName": "error message"
  }
}
```

### HTTP Status Codes
- **200**: Success
- **201**: Created
- **400**: Bad Request (validation errors)
- **401**: Unauthorized
- **403**: Forbidden
- **404**: Not Found
- **409**: Conflict (duplicate resources)
- **500**: Internal Server Error

## Development Tools

### phpMyAdmin
Access at `http://localhost:8081`
- Server: `mysql`
- Username: `root`
- Password: `root`

### MailHog
Access at `http://localhost:8025` to view captured emails
SMTP server available at `localhost:1025`

## Environment Variables

Configure these in production:
```properties
# Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}
jwt.refresh-expiration=${JWT_REFRESH_EXPIRATION:604800000}

# Server
server.port=${PORT:8080}
```

## Testing

Run tests with:
```bash
mvn test
```

## Building for Production

```bash
mvn clean package -DskipTests
java -jar target/Flyway-0.0.1-SNAPSHOT.jar
```

## Troubleshooting

### JOOQ Code Generation Fails
- Ensure MySQL is running
- Ensure database `flyway_db` exists
- Check database credentials in `pom.xml` JOOQ plugin configuration

### Application Won't Start
- Check if port 8080 is available
- Verify MySQL is running on port 3306
- Check application logs for specific errors

### JWT Token Issues
- Verify the JWT secret is properly configured
- Check token expiration times
- Ensure tokens are sent in `Authorization: Bearer {token}` format

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

[Add your license here]

