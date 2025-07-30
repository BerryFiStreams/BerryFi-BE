# BerryFi Studio Authentication API

This is the authentication API implementation for the BerryFi Studio Pixel Streaming Platform, built with Spring Boot and Spring Security.

## Features

- ✅ JWT-based authentication with access and refresh tokens
- ✅ Role-based access control (RBAC) with comprehensive permission system
- ✅ Multi-level user roles (Super Admin, Organization, Workspace)
- ✅ Secure password encryption using BCrypt
- ✅ Token refresh mechanism
- ✅ Comprehensive error handling
- ✅ H2 in-memory database for development
- ✅ Sample data initialization

## Architecture

### User Roles Hierarchy

1. **Super Admin** - Has access to everything
2. **Organization Level Roles:**
   - `ORG_OWNER` - Full organization access
   - `ORG_ADMIN` - Administrative access within organization
   - `ORG_AUDITOR` - Audit and usage monitoring
   - `ORG_REPORTER` - Analytics and reporting
   - `ORG_BILLING` - Billing management
   - `ORG_MEMBER` - Basic organization member

3. **Workspace Level Roles:**
   - `WORKSPACE_ADMIN` - Administrative access within workspace
   - `WORKSPACE_AUDITOR` - Audit and usage monitoring within workspace
   - `WORKSPACE_REPORTER` - Analytics and reporting within workspace
   - `WORKSPACE_BILLING` - Billing management within workspace
   - `WORKSPACE_MEMBER` - Basic workspace member

### Permission System

The system implements a comprehensive permission-based access control with over 50 different permissions covering:

- Authentication (login, logout, refresh, profile access)
- User management (view, create, update, delete, role management)
- Project management (CRUD operations, deployment, configuration)
- Billing (balance, usage, transactions, payment methods)
- Team management (members, campaigns, leads)
- Workspace management (creation, stats, member management)
- Analytics (usage, leads, geographic, devices, network)
- Audit (logs, stats, users, actions)
- Usage tracking (sessions, stats, workspaces, members)
- Reports (dashboard, analytics, export)

## API Endpoints

### Authentication Endpoints

- `POST /v1/api/auth/login` - User login
- `POST /v1/api/auth/refresh` - Refresh access token
- `GET /v1/api/auth/me` - Get current user profile
- `POST /v1/api/auth/logout` - User logout

### Test Endpoints

- `GET /v1/api/test/public` - Public endpoint (no auth required)
- `GET /v1/api/test/protected` - Protected endpoint (auth required)
- `GET /v1/api/test/admin` - Admin-only endpoint
- `GET /v1/api/test/permissions` - Get user permissions
- `GET /v1/api/test/billing` - Billing permissions test
- `GET /v1/api/test/reports` - Reporting permissions test

## Sample Users

The application comes with pre-configured test users:

1. **Super Admin**
   - Email: `admin@berryfi.com`
   - Password: `SuperSecurePassword@123`

2. **Organization Owner**
   - Email: `mithesh@ravgroup.org`
   - Password: `password123`

3. **Organization Admin**
   - Email: `admin@ravgroup.org`
   - Password: `password123`

4. **Workspace Admin**
   - Email: `workspace.admin@apexmarketing.com`
   - Password: `password123`

5. **Organization Member**
   - Email: `member@ravgroup.org`
   - Password: `password123`

6. **Workspace Member**
   - Email: `member@apexmarketing.com`
   - Password: `password123`

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run the application:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Testing the API

1. **Login to get tokens:**

```bash
curl -X POST http://localhost:8080/v1/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@berryfi.com",
    "password": "SuperSecurePassword@123"
  }'
```

2. **Use the access token for authenticated requests:**

```bash
curl -X GET http://localhost:8080/v1/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

3. **Test permissions:**

```bash
curl -X GET http://localhost:8080/v1/api/test/permissions \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### H2 Database Console

Access the H2 database console at: `http://localhost:8080/v1/h2-console`

- JDBC URL: `jdbc:h2:mem:berryfidb`
- Username: `sa`
- Password: `password`

## Security Configuration

- JWT tokens expire after 24 hours (configurable)
- Refresh tokens expire after 7 days (configurable)
- Passwords are encrypted using BCrypt
- CORS is configured for cross-origin requests
- Security headers are properly configured

## Project Structure

```
src/main/java/com/berryfi/portal/
├── config/              # Configuration classes
│   ├── SecurityConfig.java
│   └── DataInitializer.java
├── controller/          # REST controllers
│   ├── AuthController.java
│   └── TestController.java
├── dto/                # Data Transfer Objects
│   ├── auth/
│   ├── user/
│   └── error/
├── entity/             # JPA entities
│   └── User.java
├── enums/              # Enumerations
│   ├── Role.java
│   ├── Permission.java
│   ├── AccountType.java
│   └── UserStatus.java
├── exception/          # Custom exceptions
│   ├── AuthenticationException.java
│   └── GlobalExceptionHandler.java
├── repository/         # Data repositories
│   └── UserRepository.java
├── security/           # Security components
│   └── JwtAuthenticationFilter.java
└── service/            # Business logic services
    ├── AuthService.java
    ├── JwtService.java
    ├── CustomUserDetailsService.java
    └── PermissionService.java
```

## Future Enhancements

- [ ] Database migration to PostgreSQL/MySQL for production
- [ ] User registration and email verification
- [ ] Password reset functionality
- [ ] Rate limiting for authentication endpoints
- [ ] Audit logging for security events
- [ ] Multi-factor authentication (MFA)
- [ ] Session management and concurrent login control
- [ ] API documentation with OpenAPI/Swagger

## Technologies Used

- **Spring Boot 3.5.3** - Application framework
- **Spring Security 6** - Security framework
- **Spring Data JPA** - Data persistence
- **JWT (JSON Web Tokens)** - Authentication tokens
- **H2 Database** - In-memory database for development
- **BCrypt** - Password encryption
- **Maven** - Build tool
- **Java 17** - Programming language

## License

This project is part of the BerryFi Studio platform.
