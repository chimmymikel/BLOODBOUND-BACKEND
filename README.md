# 🩸 BloodBound — Backend API

> RESTful API backend for BloodBound — a localized blood donation coordination platform connecting verified donors with individuals in urgent need across Cebu City and surrounding areas.

**🔗 GitHub:** [https://github.com/chimmymikel/BLOODBOUND-BACKEND](https://github.com/chimmymikel/BLOODBOUND-BACKEND)

**🚀 Live API:** [https://bloodbound-backend.onrender.com](https://bloodbound-backend.onrender.com)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Environment Configuration](#environment-configuration)
- [API Reference](#api-reference)
- [Security Architecture](#security-architecture)
- [Database Design](#database-design)
- [Business Logic](#business-logic)
- [Event System](#event-system)
- [Error Handling](#error-handling)
- [Docker](#docker)
- [Related Repositories](#related-repositories)

---

## Overview

BloodBound Backend is a **Spring Boot 3.x RESTful API** that powers both the React web application and the Kotlin Android mobile app. It handles all business logic including JWT authentication, donor eligibility tracking, atomic commitment transactions, and request lifecycle management.

The full BloodBound system comprises:
- **This Repo** — Java Spring Boot API + PostgreSQL
- **Web Frontend** — React/TypeScript ([bloodbound-webapp.vercel.app](https://bloodbound-webapp.vercel.app/))
- **Mobile** — Native Android app (Kotlin/Jetpack Compose)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 LTS |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (HMAC SHA-256) |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL 14+ |
| Build Tool | Maven (with Maven Wrapper) |
| Containerization | Docker |
| Deployment | Render |

---

## Project Structure

```
src/
└── main/
    ├── java/com/bloodbound/backend/
    │   ├── commitment/
    │   │   ├── CancelResult.java
    │   │   ├── Commitment.java
    │   │   ├── CommitmentController.java
    │   │   ├── CommitmentEventListener.java
    │   │   ├── CommitmentRepository.java
    │   │   ├── CommitmentResult.java
    │   │   ├── CommitmentService.java
    │   │   └── CreateCommitmentRequest.java
    │   ├── common/
    │   │   ├── ApiResponse.java
    │   │   ├── DonationCompletedEvent.java
    │   │   ├── GlobalExceptionHandler.java
    │   │   └── RequestFulfilledEvent.java
    │   ├── hospital/
    │   │   ├── DataSeeder.java
    │   │   ├── Hospital.java
    │   │   ├── HospitalController.java
    │   │   └── HospitalRepository.java
    │   ├── identity/
    │   │   ├── AuthController.java
    │   │   ├── AuthRequest.java
    │   │   ├── HealthController.java
    │   │   ├── JwtFilter.java
    │   │   ├── JwtService.java
    │   │   ├── PasswordUpdateRequest.java
    │   │   ├── ProfileController.java
    │   │   ├── ProfileUpdateRequest.java
    │   │   ├── SecurityConfig.java
    │   │   ├── User.java
    │   │   ├── UserRepository.java
    │   │   └── UserStatsListener.java
    │   ├── request/
    │   │   ├── CreateRequestRequest.java
    │   │   ├── FulfillResult.java
    │   │   ├── Request.java
    │   │   ├── RequestController.java
    │   │   ├── RequestRepository.java
    │   │   ├── RequestResponse.java
    │   │   └── RequestService.java
    │   └── BackendApplication.java
    └── resources/
        ├── application.properties
        ├── application-local.properties
        └── application-local.properties.example
```

### Package Responsibilities

| Package | Responsibility |
|---|---|
| `commitment` | Donation commitment lifecycle — create, cancel, event handling, and atomic transactions |
| `common` | Shared infrastructure — unified API response wrapper, global exception handler, and domain events |
| `hospital` | Hospital entity, repository, REST controller, and Cebu hospital data seeding |
| `identity` | Authentication, JWT filter/service, user entity, profile management, and Spring Security config |
| `request` | Blood request CRUD, fulfillment logic, DTO mapping, and request status management |

---

## Getting Started

### Prerequisites

- Java 17 LTS
- Maven 3.8+ (or use the included `./mvnw` wrapper)
- PostgreSQL 14+
- Docker (optional)

### Local Setup

```bash
# Clone the repository
git clone https://github.com/chimmymikel/BLOODBOUND-BACKEND.git
cd BLOODBOUND-BACKEND

# Copy the example config and fill in your values
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties

# Run with local profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The API will be available at `http://localhost:8080`.

### Build JAR

```bash
./mvnw clean package -DskipTests
java -jar target/backend-*.jar
```

---

## Environment Configuration

The project uses a profile-based configuration strategy. Sensitive credentials go in `application-local.properties` (git-ignored). Use `application-local.properties.example` as a template.

### `application-local.properties`

```properties
# PostgreSQL Database
spring.datasource.url=jdbc:postgresql://localhost:5432/bloodbound
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# JWT
jwt.secret=your_hmac_sha256_secret_key
jwt.expiration=86400000

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

> ⚠️ **Never commit** `application-local.properties`. It is already listed in `.gitignore`.

---

## API Reference

**Base URL:** `https://bloodbound-backend.onrender.com/api`

All responses follow the unified `ApiResponse<T>` wrapper:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-05-23T12:00:00Z"
}
```

---

### Authentication Endpoints (`/api/auth`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Register a new Donor or Requester |
| `POST` | `/auth/login` | Public | Authenticate and receive JWT |
| `GET` | `/auth/me` | Authenticated | Get current user profile and eligibility |

#### `POST /auth/register`
```json
// Request Body
{
  "email": "john@example.com",
  "password": "Password123",
  "confirmPassword": "Password123",
  "fullName": "John Doe",
  "bloodType": "O_POSITIVE",
  "role": "DONOR",
  "lastDonationDate": "2026-01-01"
}

// Response 201
{
  "user": { "id": "...", "email": "...", "fullName": "...", "bloodType": "...", "role": "DONOR" },
  "token": "<JWT>"
}
```

#### `POST /auth/login`
```json
// Request Body
{ "email": "john@example.com", "password": "Password123" }

// Response 200
{
  "user": { "id": "...", "email": "...", "fullName": "...", "role": "DONOR", "totalDonations": 3 },
  "token": "<JWT>"
}
```

---

### Blood Request Endpoints (`/api/requests`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/requests` | Authenticated | List active requests with filters |
| `GET` | `/requests/{id}` | Authenticated | Get specific request with committed donors |
| `POST` | `/requests` | Requester | Post a new blood request |
| `PATCH` | `/requests/{id}/fulfill` | Requester | Mark request as fulfilled |

#### `GET /requests` — Query Parameters
```
?page=0&size=20&status=ACTIVE&bloodType=A_POSITIVE&urgency=CRITICAL
```

#### `POST /requests`
```json
// Request Body
{
  "bloodType": "A_POSITIVE",
  "hospitalId": "ccmc-001",
  "unitsNeeded": 2,
  "urgency": "URGENT",
  "patientDetails": "Severe anemia, Room 304"
}

// Response 201
{ "request": { "id": "...", "status": "ACTIVE", "createdAt": "..." } }
```

---

### Commitment Endpoints (`/api/commitments`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/commitments` | Donor | Commit to donate for a request |
| `GET` | `/commitments` | Authenticated | List commitments by donor or request |
| `DELETE` | `/commitments/{id}` | Donor | Cancel an existing commitment |

#### `POST /commitments`
```json
// Request Body
{ "requestId": "request-uuid-123" }

// Response 201
{
  "commitment": {
    "id": "...",
    "status": "PENDING",
    "hospitalDetails": {
      "name": "Cebu City Medical Center",
      "address": "Osmena Blvd, Cebu City",
      "phone": "032-255-5555"
    }
  }
}
```

---

### Eligibility & Profile Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/users/{id}/eligibility` | Authenticated | Check 56-day eligibility status |
| `PUT` | `/profile` | Authenticated | Update user profile |
| `PUT` | `/profile/password` | Authenticated | Update password |
| `GET` | `/hospitals` | Authenticated | List all verified Cebu hospitals |
| `GET` | `/health` | Public | Health check endpoint |

#### `GET /users/{id}/eligibility`
```json
// Response 200
{
  "isEligible": false,
  "daysUntilEligible": 26,
  "nextEligibleDate": "2026-06-18",
  "message": "Eligible in 26 days"
}
```

---

## Security Architecture

### JWT Authentication

- Algorithm: **HMAC SHA-256**
- Expiry: **24 hours** (configurable via `jwt.expiration`)
- Transmission: `Authorization: Bearer <token>` header
- Filter: `JwtFilter` intercepts all protected routes and validates the token via `JwtService`

### Password Security

- Hashing: **BCrypt** (strength 12)
- Password updates handled separately via `PasswordUpdateRequest` through `ProfileController`

### Role-Based Access Control (RBAC)

| Role | Permissions |
|---|---|
| `DONOR` | Browse requests, commit to donate, view eligibility, manage own commitments |
| `REQUESTER` | Post blood requests, mark requests fulfilled, view committed donors |

RBAC is enforced at the controller level using Spring Security's `@PreAuthorize` annotations and `SecurityConfig`.

### Transaction Integrity

Commitment creation uses **SERIALIZABLE isolation** with row-level locking to prevent:
- Duplicate commitments (`UNIQUE` constraint on `request_id, donor_id`)
- Race conditions during concurrent donor commits
- Partial state from failed fulfillment operations

---

## Database Design

### Entity Summary

#### `users`
| Column | Type | Notes |
|---|---|---|
| `id` | String PK | UUID |
| `email` | String | UNIQUE |
| `password_hash` | String | BCrypt |
| `full_name` | String | |
| `blood_type` | String | Enum |
| `role` | String | DONOR / REQUESTER |
| `last_donation_date` | Date | Used for eligibility |
| `total_donations` | Int | Incremented on fulfillment |
| `created_at` | Timestamp | |

#### `requests`
| Column | Type | Notes |
|---|---|---|
| `id` | String PK | UUID |
| `requester_id` | String FK | → users |
| `hospital_id` | String FK | → hospitals |
| `blood_type` | String | |
| `units_needed` | Int | 1–10 |
| `urgency` | String | NORMAL / URGENT / CRITICAL |
| `status` | String | ACTIVE → FULFILLED → ARCHIVED |
| `patient_details` | String | |
| `created_at` | Timestamp | |

#### `commitments`
| Column | Type | Notes |
|---|---|---|
| `id` | String PK | UUID |
| `request_id` | String FK | → requests |
| `donor_id` | String FK | → users |
| `status` | String | PENDING / COMPLETED / CANCELLED |
| `committed_at` | Timestamp | |

UNIQUE constraint on `(request_id, donor_id)` — prevents duplicate commits.

#### `hospitals`
| Column | Type | Notes |
|---|---|---|
| `id` | String PK | |
| `name` | String | |
| `address` | String | |
| `latitude` | Float | |
| `longitude` | Float | |
| `phone` | String | |

Hospital records are seeded at startup via `DataSeeder.java` with verified Cebu City medical facilities.

### Relationships

- `User (Requester)` → `Requests` — one-to-many
- `Hospital` → `Requests` — one-to-many
- `User (Donor)` → `Commitments` — one-to-many
- `Request` → `Commitments` — one-to-many

---

## Business Logic

### 56-Day Eligibility Rule

```java
daysRemaining = 56 - ChronoUnit.DAYS.between(lastDonationDate, LocalDate.now())
isEligible    = daysRemaining <= 0
```

Handled by the eligibility endpoint. If `lastDonationDate` is null (first-time donor), the donor is immediately eligible.

### Request Fulfillment Workflow (`PATCH /requests/{id}/fulfill`)

When a Requester marks a request as fulfilled, the backend executes a single atomic transaction that:

1. Updates request `status` → `FULFILLED`
2. Sets `fulfilled_at` timestamp
3. Updates all committed donors' `last_donation_date` to today
4. Increments each committed donor's `total_donations` count
5. Publishes `RequestFulfilledEvent` and `DonationCompletedEvent`

After fulfillment, all committed donors immediately see their eligibility status flip to ineligible with a fresh 56-day countdown.

### Request Status Lifecycle

```
ACTIVE → FULFILLED → ARCHIVED
```

Transitions are enforced; a donor cannot commit to a `FULFILLED` or `ARCHIVED` request (returns error code `REQ-001`).

---

## Event System

The backend uses Spring's application event system for decoupled post-fulfillment logic.

| Event | Publisher | Listener | Effect |
|---|---|---|---|
| `RequestFulfilledEvent` | `RequestService` (on fulfill) | `CommitmentEventListener` | Updates commitment statuses to COMPLETED |
| `DonationCompletedEvent` | `CommitmentEventListener` | `UserStatsListener` | Updates donor `last_donation_date` and `total_donations` |

This keeps the fulfillment transaction focused and allows listener logic to be extended without modifying core service code.

---

## Error Handling

All exceptions are handled centrally by `GlobalExceptionHandler` and returned in the standard `ApiResponse` format.

### HTTP Status Codes

| Code | Meaning |
|---|---|
| `200 OK` | Request successful |
| `201 Created` | Resource created (User, Request, Commitment) |
| `400 Bad Request` | Validation or business logic failure |
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Authenticated but wrong role |
| `404 Not Found` | Resource ID does not exist |
| `409 Conflict` | Duplicate resource (e.g. double commitment) |

### Application Error Codes

| Code | Message | Description |
|---|---|---|
| `AUTH-001` | Invalid Credentials | Wrong email or password |
| `AUTH-002` | Token Expired | JWT has expired; re-login required |
| `VALID-001` | Validation Failed | Input does not meet requirements |
| `ELIG-001` | Ineligible Donor | Within the 56-day post-donation window |
| `REQ-001` | Request Closed | Attempting to commit to a fulfilled/cancelled request |
| `DB-001` | Resource Not Found | ID does not exist in the database |
| `SYS-001` | Internal Server Error | Unexpected server error |

---

## Docker

A `Dockerfile` is included and is used by **Render** for containerized deployment.

```bash
# Build the image locally
docker build -t bloodbound-backend .

# Run the container locally
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/bloodbound \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=pass \
  -e JWT_SECRET=your_secret \
  bloodbound-backend
```

### Render Deployment

The backend is deployed on **Render** using the included `Dockerfile`.

**🚀 Live API:** [https://bloodbound-backend.onrender.com](https://bloodbound-backend.onrender.com)

To deploy your own instance on Render:

1. Push the repository to GitHub
2. Create a new **Web Service** on [render.com](https://render.com)
3. Connect your GitHub repository
4. Set **Environment** to `Docker`
5. Add the following environment variables in the Render dashboard:

| Key | Value |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>:<port>/<db>` |
| `SPRING_DATASOURCE_USERNAME` | your DB username |
| `SPRING_DATASOURCE_PASSWORD` | your DB password |
| `JWT_SECRET` | your HMAC SHA-256 secret |
| `SPRING_PROFILES_ACTIVE` | `prod` |

> ⚠️ **Note:** Render free-tier services spin down after inactivity. The first request after a cold start may take 30–60 seconds to respond.

---

## Related Repositories

| Component | Stack | Link |
|---|---|---|
| `bloodbound-webapp` | React 18, TypeScript, Tailwind, Vercel | [bloodbound-webapp.vercel.app](https://bloodbound-webapp.vercel.app/) |
| `bloodbound-android` | Kotlin, Jetpack Compose, Retrofit, Room | Android APK |

---

## Academic Context

This project is part of **IT342 — System Integration and Architecture (G7)** at **Cebu Institute of Technology — University (CIT-U)**.

- **Prepared by:** Michelle Marie Palacio Habon
- **SDD Version:** 2.0 (Final — 02/16/2026)
- **Backend Base Package:** `com.bloodbound.backend`

---

*BloodBound — Connecting blood donors to those who need it most, across Cebu City.*
