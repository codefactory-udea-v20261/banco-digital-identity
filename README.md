# Banco Digital - Identity Service

The Identity Service handles authentication, authorization, user provisioning, and JWT token management for the Banco Digital platform.

## Architecture

- **Hexagonal/Clean Architecture** - Domain-driven design with ports and adapters
- **JWT-based Authentication** - Stateless token issuance and validation
- **BCrypt** - Password hashing with cost factor 12
- **Token Revocation** - JPA-based blacklist for logout/security
- **MFA Support** - Multi-factor authentication for privileged roles
- **Account Lockout** - Automatic lockout after failed login attempts

## Running Locally

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 16+
- Docker (optional)

### Build

```bash
mvn clean package -DskipTests
```

### Run

```bash
# Set environment variables
export APP_PROFILE=local
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=banco_digital_identity
export DB_USERNAME=auth_user
export DB_PASSWORD=your_password
export JWT_SECRET=your_base64_encoded_256bit_secret_here

mvn spring-boot:run
```

### Docker

```bash
docker build -t banco-digital-identity .
docker run -p 8081:8081 \
  -e APP_PROFILE=local \
  -e DB_HOST=localhost \
  -e DB_NAME=banco_digital_identity \
  -e DB_USERNAME=auth_user \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_secret \
  banco-digital-identity
```

## Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `APP_PROFILE` | Spring profile | `local` |
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `banco_digital_identity` |
| `DB_USERNAME` | Database user | `auth_user` |
| `DB_PASSWORD` | Database password | *(required)* |
| `JWT_SECRET` | Base64-encoded 256-bit secret | *(required)* |
| `JWT_EXPIRATION_MS` | Token expiration | `3600000` |

## API Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/v1/auth/login` | Authenticate user | No |
| POST | `/api/v1/auth/register` | Register new user | No |
| POST | `/api/v1/auth/logout` | Revoke token | Yes |
| POST | `/api/v1/auth/refresh` | Refresh token | Yes |
| POST | `/api/v1/auth/verify-mfa` | Verify MFA code | No |
| GET | `/api/v1/auth/me` | Get current user | Yes |
| POST | `/api/v1/auth/provision-client-access` | Provision client access | Internal |
| GET | `/api/v1/auth/exists/{email}` | Check email exists | Internal |

## Swagger UI

Available at: `http://localhost:8081/swagger-ui.html`

## Roles

| Role | ID | Description |
|------|----|-------------|
| ADMIN | 1 | Full administrative access |
| CAJERO | 2 | Teller/cashier operations |
| CLIENTE | 3 | Standard customer access |
| AUDITOR | 4 | Read-only audit access |

## Testing

```bash
# Unit and integration tests
mvn test

# With Testcontainers
mvn verify -P test
```
