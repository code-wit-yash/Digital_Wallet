# Digital Wallet Backend

A secure, RESTful digital wallet backend built using **Java, Spring Boot, Spring Security, JWT, and MySQL**. The application supports user authentication, wallet operations, peer-to-peer transfers, transaction history, double-entry ledger records, and administrative ledger reconciliation.

## Features

- **Authentication & Authorization:** User registration and login with BCrypt password hashing and JWT-based authentication.
- **Wallet Management:** Automatically creates a wallet for registered users and allows users to view their wallet balance.
- **Deposit & Withdrawal:** Supports wallet deposits and withdrawals with transaction validation.
- **Peer-to-Peer Transfers:** Transfers funds between users using their registered email addresses.
- **Idempotent Transfers:** Supports idempotency keys to prevent duplicate transfers when a request is retried.
- **Transaction History:** Retrieves wallet transaction records.
- **Double-Entry Ledger:** Records debit and credit entries for wallet operations and transfers.
- **Concurrency Protection:** Uses database locking to help prevent conflicting concurrent wallet updates.
- **Ledger Reconciliation:** Provides administrative endpoints to identify successful transactions missing ledger entries and unbalanced transfers.
- **API Documentation:** Interactive API documentation through Swagger UI.
- **Exception Handling:** Centralized handling of application errors and validation failures.
- **Audit Logging:** Records relevant application events for traceability.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 23 | Programming language |
| Spring Boot 4 | Backend framework |
| Spring Security | Authentication and authorization |
| JWT | Stateless authentication |
| Spring Data JPA / Hibernate | Database access and ORM |
| MySQL | Relational database |
| Maven | Dependency management and build |
| Swagger / OpenAPI | API documentation |
| Postman | API testing |

## Project Structure

The application follows a layered, modular structure:

```text
src/main/java/org/example/
├── configuration/
├── security/
├── user/
├── wallet/
├── transaction/
└── ...
```

The modules separate responsibilities such as configuration, security, user management, wallet operations, and transaction processing.

*Note: Update this tree to match the actual package structure before publishing.*

## Getting Started

### Prerequisites

- JDK 23
- MySQL Server
- Maven, or the included Maven Wrapper
- Postman (optional, for API testing)

### 1. Clone the repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd Digital_Wallet
```

Replace the repository URL with your actual GitHub repository URL.

### 2. Create the database

Open MySQL and create a database:

```sql
CREATE DATABASE digital_wallet;
```

### 3. Configure the application

Update `src/main/resources/application.properties` with your local MySQL connection details and the configuration required by the application.

Example database configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/digital_wallet
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Configure the JWT secret and any other required properties using the names expected by your application. **Do not commit database passwords, JWT secrets, or real access tokens to GitHub.**

### 4. Run the application

Using Maven:

```bash
mvn spring-boot:run
```

Or, if the project includes the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The default local API address is:

```text
http://localhost:8080
```

## API Endpoints

All endpoints below use the local base URL `http://localhost:8080`.

### Authentication

| Method | Endpoint | Description | Authentication |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | Not required |
| POST | `/api/auth/login` | Authenticate and obtain a JWT | Not required |
| GET | `/api/auth/me` | Get the authenticated user's details | JWT |

### Wallet

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/wallet/me` | View the current user's wallet |
| POST | `/api/wallet/deposit` | Deposit funds |
| POST | `/api/wallet/withdraw` | Withdraw funds |
| GET | `/api/wallet/transactions` | View transaction history |
| POST | `/api/wallet/transfer` | Transfer funds to another user |

Wallet endpoints require authentication unless explicitly configured otherwise.

### Administrative Ledger Reconciliation

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/reconciliation/missing-ledger-entries` | Find successful transactions without ledger entries |
| GET | `/api/reconciliation/unbalanced-transfers` | Find transfer ledger groups whose debit and credit totals do not match |

Both reconciliation endpoints require an authenticated user with the `ADMIN` role.

## Authentication

1. Register a user through `/api/auth/register`.
2. Log in through `/api/auth/login`.
3. Copy the JWT returned by the login endpoint.
4. Include it in protected API requests using the following header:

```http
Authorization: Bearer <YOUR_JWT_TOKEN>
```

The application uses BCrypt for password hashing and JWT-based authentication for stateless API access.

## Idempotent Transfers

The transfer endpoint accepts an `Idempotency-Key` header.

Example:

```http
POST /api/wallet/transfer
Idempotency-Key: transfer-demo-001
Authorization: Bearer <YOUR_JWT_TOKEN>
Content-Type: application/json
```

Example request body:

```json
{
  "recipientEmail": "recipient@example.com",
  "amount": 400
}
```

Use the **same idempotency key when retrying the same transfer**. Use a new key for a separate transfer. This helps avoid duplicate processing caused by repeated requests.

## Double-Entry Ledger

The ledger records debit and credit entries for wallet operations and transfers.

- Deposits record entries between the external clearing account and the wallet.
- Withdrawals record entries between the wallet and the external clearing account.
- Transfers record the corresponding sender and recipient wallet entries.
- Reconciliation endpoints help identify missing ledger entries and transfer groups whose debit and credit totals do not balance.

Historical transactions created before ledger recording was introduced may appear in the missing-ledger-entry report. A reported missing entry is an item to investigate, not proof that a transaction should automatically receive a new ledger entry.

## API Testing with Postman

The repository includes a Postman collection named `Digital_Wallet_Backend_API`.

1. Import the collection JSON into Postman.
2. Start the backend and ensure MySQL is running.
3. Register a test user and log in.
4. Use the returned JWT for protected requests.
5. Test wallet operations and transfers.
6. Log in as an administrator to test the reconciliation endpoints.

The collection uses the local base URL `http://localhost:8080`. Replace example credentials and recipient details with your own test data. Never export or publish real passwords or JWT tokens.

## Error Handling

The application includes centralized exception handling and request validation. Protected endpoints require valid authentication, and administrative reconciliation endpoints enforce role-based access.

## Future Improvements

- Integration tests for complete wallet workflows
- Concurrent transfer and idempotency tests
- Automated ledger integrity checks
- Improved operational monitoring and audit reliability

Redis Integration (Optional)

Redis caching support has been added to the project, but it is currently disabled because a Redis server connection has not been established. The related Redis configuration in application.properties and the Redis-related code are commented out, so the application can run without Redis.

How to Enable Redis

Follow these steps to establish a Redis connection and enable the integration:

Install Redis: Install Redis locally or use Docker to run a Redis server.

docker run --name digital-wallet-redis -p 6379:6379 -d redis

Verify the connection: Ensure the Redis container is running and accessible on localhost:6379.

docker ps

Enable the Redis dependency: Verify that the Redis dependency, such as spring-boot-starter-data-redis, is present in pom.xml. Add it if it is missing.

Update application.properties: Uncomment the Redis configuration and ensure the host and port match the running Redis server. For a local instance, the typical settings are:

**Configure environment variables.** The application supports the following Redis settings:

   | Variable | Default / Purpose |
   |---|---|
   | `REDIS_HOST` | `localhost` |
   | `REDIS_PORT` | `6379` |
   | `REDIS_PASSWORD` | Empty by default |
   | `REDIS_SSL_ENABLED` | `false` by default |

**Enable Spring caching.** Change the following property in `application.properties`:

   ```properties
   spring.cache.type=redis
   ```

Uncomment the Redis-related code: Re-enable the Redis configuration, cache configuration, and any Redis-related services that were commented out. Ensure the required beans and dependencies are correctly configured.

Restart and test: Restart the Spring Boot application and verify that the Redis connection succeeds. Test the relevant caching operations to confirm that Redis is working as expected.

Note: Redis integration is currently optional and has not been verified end-to-end in the active application configuration. The exact steps may vary depending on the Redis configuration and code present in the project.

## Author

**Yash Ashok Jawale**

Information Technology Graduate | Java Backend Development

