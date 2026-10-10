# Digital Wallet & Payment Processing Platform

A backend-first digital wallet application built with Java and Spring Boot, supporting secure authentication, wallet management, and peer-to-peer transfers.

## Features

- **Authentication & Authorization:** JWT-based authentication, Spring Security, and BCrypt password hashing.
- **Wallet Management:** Automatic wallet creation during user registration, with INR balances.
- **Deposits & Withdrawals:** Validated wallet operations with transaction records.
- **Peer-to-Peer Transfers:** Transfers between registered users with debit and credit transaction records.
- **Transaction History:** Retrieve wallet transactions with timestamps, amounts, types, and statuses.
- **Concurrency Protection:** Pessimistic wallet locking to protect balance updates.
- **Idempotent Transfers:** Repeated requests using the same idempotency key return the original transaction; reusing the key for a different request is rejected.
- **Database Transactions:** Transactional service methods to keep related wallet updates consistent.

## Technology Stack

Java 23 · Spring Boot · Spring Security · Spring Data JPA · Hibernate · MySQL · JWT · Maven · Postman

## Running Locally

1. Install Java 23 and MySQL.
2. Create a MySQL database for the application.
3. Configure the database connection and JWT secret in `application.properties`.
4. Run the application from IntelliJ IDEA or using the Maven wrapper, if included.
5. Test the endpoints using Postman and a valid JWT.

## Main API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Authenticate and obtain a JWT |
| GET | `/api/auth/me` | Verify authenticated access |
| GET | `/api/wallet/me` | Retrieve the current wallet |
| POST | `/api/wallet/deposit` | Deposit funds |
| POST | `/api/wallet/withdraw` | Withdraw funds |
| POST | `/api/wallet/transfer` | Transfer funds to another user |
| GET | `/api/wallet/transactions` | View transaction history |

**Transfer requests:** Include the `Authorization: Bearer <token>` header and a unique `Idempotency-Key` header. Retrying the same request with the same key returns the original transaction.

## Scope

This is a learning and portfolio project. Deposits are simulated; the application is not connected to a real banking or payment network.
