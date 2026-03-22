# config-tracker

A small **Spring Boot** REST service that records **configuration changes** (for example credit limits or approval policies). It exposes APIs to create and query changes, applies **request and business validation**, and **simulates** notifying an external monitoring system when a change is marked **critical**.

**Stack:** Java 21, Spring Boot 3.5.x, Maven. Base package: `com.ohpen.configtracker`.

---

## How to run

### Prerequisites

- **JDK 21**
- **Maven** (optional if you use the included wrapper)

### Start the application

From the project root:

```bash
./mvnw spring-boot:run
```

On Windows:

```cmd
mvnw.cmd spring-boot:run
```

The app listens on the default port **8080** unless overridden.

### Health check

Spring Boot Actuator is included. After startup:

```http
GET http://localhost:8080/actuator/health
```

---

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/config-changes` | Create a configuration change record. |
| `GET` | `/api/config-changes` | List all changes, optionally filtered by `type`, `from`, and `to`. |
| `GET` | `/api/config-changes/{id}` | Get a single change by UUID. |

**Query parameters (GET list, all optional):**

| Parameter | Type | Description |
|-----------|------|-------------|
| `type` | `ADD`, `UPDATE`, or `DELETE` | Keep only changes of this type. |
| `from` | ISO-8601 date-time | Keep changes with `changedAt` ≥ `from`. |
| `to` | ISO-8601 date-time | Keep changes with `changedAt` ≤ `to`. |

---

## Example requests and responses

### Create a config change

**Request**

```http
POST /api/config-changes
Content-Type: application/json
```

```json
{
  "ruleName": "max-credit-limit",
  "type": "UPDATE",
  "oldValue": "50000",
  "newValue": "75000",
  "critical": true,
  "changedBy": "policy-admin",
  "reason": "Q1 policy revision"
}
```

**Response** `200 OK` — body is the persisted entity (JSON):

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "changedAt": "2026-03-22T12:00:00.123456789Z",
  "ruleName": "max-credit-limit",
  "type": "UPDATE",
  "oldValue": "50000",
  "newValue": "75000",
  "critical": true,
  "changedBy": "policy-admin",
  "reason": "Q1 policy revision"
}
```

If `critical` is `true`, the service invokes `MonitoringNotifier` (default implementation: **INFO** log line for the change id).

---

### List config changes

**Request**

```http
GET /api/config-changes
```

**Response** `200 OK` — JSON array of `ConfigChange` objects.

---

### Filter by type and time

**Request**

```http
GET /api/config-changes?type=UPDATE&from=2026-03-01T00:00:00Z&to=2026-03-31T23:59:59Z
```

**Response** `200 OK` — array of changes matching **all** supplied filters (type match, `changedAt` within the inclusive window).

---

### Get by id

**Request**

```http
GET /api/config-changes/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response** `200 OK` — single `ConfigChange` object, or **404** if the id is unknown (see below).

---

### Error responses

Errors use a consistent JSON shape:

```json
{
  "message": "Human-readable explanation",
  "timestamp": "2026-03-22T12:00:00.123456789Z"
}
```

Typical cases:

| Situation | HTTP status | `message` (illustrative) |
|-----------|-------------|---------------------------|
| Bean validation failure (e.g. blank `ruleName`) | **400** | First field error, e.g. `ruleName must not be blank` |
| Business rule violation (`InvalidConfigChangeException`) | **400** | e.g. `For UPDATE, oldValue and newValue must not be equal` |
| Unknown id (`ConfigChangeNotFoundException`) | **404** | `Config change not found for id: …` |
| Unhandled server error | **500** | `Unexpected error occurred` |

---

## Design decisions

- **In-memory storage** — A `ConcurrentHashMap`-backed repository keeps the assignment focused on API design, validation, and layering without database setup. It is easy to run and test locally.
- **Two layers of validation** — **Jakarta Bean Validation** on `CreateConfigChangeRequest` enforces structural rules at the edge (non-blank `ruleName`, `changedBy`, `reason`; non-null `type`). **Service-layer rules** depend on `ChangeType` (e.g. ADD requires `newValue`; UPDATE requires differing old/new values). That split keeps HTTP concerns in the DTO and domain rules next to the use case.
- **Simple repository contract** — The interface exposes `save`, `findById`, and `findAll` only. Listing filters are applied in the **service** with streams so the repository stays a thin persistence abstraction and can be swapped later (e.g. for JPA) without duplicating query logic prematurely.
- **`MonitoringNotifier` interface** — Critical-change notification is behind an interface with a **logging** implementation. That keeps the service testable (mock the notifier) and leaves a clear seam for a real HTTP client or message publisher in production.

---

## Trade-offs and limitations

- Data is **not persistent**; restarts clear all records.
- **No authentication or authorization**.
- **No pagination or sorting** on list; large result sets load entirely in memory.
- The monitoring integration is a **stub** (log only): **no retries, timeouts, or circuit breaking**.
- List filtering loads **all** entities then filters in memory (acceptable for a demo, not for big datasets).

---

## What I would improve for production

- **Persistent store** (e.g. PostgreSQL + Spring Data JPA or JDBC) with migrations.
- **Structured logging**, request **correlation IDs**, and integration with a log/metrics stack.
- **Resilient outbound calls** for real monitoring (retries, backoff, circuit breaker, dead-letter handling).
- **Metrics and tracing** (Micrometer, OpenTelemetry) and dashboards.
- **Pagination, sorting, and index-backed queries** for list endpoints.
- **Security** (OAuth2 / API keys), rate limiting, and input size limits where appropriate.

---

## Tests

```bash
./mvnw test
```

The suite includes **unit tests** for `ConfigChangeService` (mocked repository and notifier) and **MockMvc integration tests** against the REST API.
