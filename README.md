# Config Tracker Service (Ohpen Assignment)

## Description

REST API for **tracking configuration changes** in a system (for example credit limits or approval policies). Clients can record changes with structured input, **validate** them at the boundary and in the **service layer**, query history with optional filters, and retrieve a single change by id. Changes marked **critical** trigger a **monitoring notification** (implemented as a logging integration suitable for swapping to a real client later).

---

## Tech Stack

- Java 21
- Spring Boot 3
- Maven

---

## How to Run

### Prerequisites

- **Java 21** (JDK)

### Start the application

From the project root:

```bash
./mvnw spring-boot:run
```

The application listens on **port 8080** by default.

### Health check

```text
http://localhost:8080/actuator/health
```

---

## Bonus Features

- **Custom metrics** — `GET /metrics` exposes lightweight, application-specific aggregates (total changes, critical count, counts by `ChangeType`) computed from the in-memory repository. This is separate from Spring Boot Actuator’s Micrometer endpoint under `/actuator/metrics`.
- **Correlation ID tracing** — A servlet filter accepts optional request header **`X-Correlation-Id`**. If it is missing or blank, a new UUID is generated. The value is stored in SLF4J **MDC** under `correlationId`, echoed on the **response** as `X-Correlation-Id`, and cleared after the request in a `finally` block (no change to controller or service code).
- **Monitoring notifier retries** — `LoggingMonitoringNotifier` uses **Spring Retry** (`@EnableRetry`, `@Retryable`): up to **3** attempts with **200 ms** fixed backoff on `RuntimeException`. A **`@Recover`** method logs when all attempts fail. For demonstration only: if **`reason`** contains **`fail`** (case-insensitive), the notifier throws to simulate instability; normal payloads succeed after the INFO log.
- **Additional validation** — The service enforces stricter **edge cases** for create and list: e.g. `ADD` requires `oldValue` to be null or blank; `DELETE` requires `newValue` to be null or blank; when both `from` and `to` are supplied on the list endpoint, **`from` must not be after `to`**.

---

## API Endpoints

### POST `/api/config-changes`

**Description:** Creates a new configuration change. The server assigns `id` and `changedAt`. Successful creation returns **HTTP 201 Created** with the saved entity in the body.

**Example request**

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

**Example response** (201 Created)

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

`type` must be one of: `ADD`, `UPDATE`, `DELETE`. Business rules depend on the type (for example: `ADD` requires a non-blank `newValue` and `oldValue` must be null or blank; `DELETE` requires a non-blank `oldValue` and `newValue` must be null or blank; `UPDATE` requires non-blank `oldValue` and `newValue` and they must differ).

---

### GET `/api/config-changes`

**Description:** Returns all stored configuration changes as a JSON array. Optional query parameters filter the result **in the service layer** after loading from the repository.

**Filtering**

| Parameter | Meaning |
|-----------|---------|
| `type` | `ADD`, `UPDATE`, or `DELETE` — only matching records |
| `from` | ISO-8601 date-time — `changedAt` must be **≥** `from` |
| `to` | ISO-8601 date-time — `changedAt` must be **≤** `to` |

**Example queries**

```http
GET /api/config-changes
```

```http
GET /api/config-changes?type=UPDATE
```

```http
GET /api/config-changes?type=ADD&from=2026-03-01T00:00:00Z&to=2026-03-31T23:59:59Z
```

**Example response** (200 OK) — abbreviated:

```json
[
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
]
```

---

### GET `/api/config-changes/{id}`

**Description:** Returns a single configuration change by UUID.

**Example**

```http
GET /api/config-changes/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response** — 200 OK with the same JSON shape as a single element in the list above, or **404** if no record exists for that id.

---

## Error Handling

A **`@RestControllerAdvice`** (`GlobalExceptionHandler`) maps exceptions to HTTP status codes and a small JSON body **`ErrorResponse`**: `message` and `timestamp` (UTC instant).

**Example error response**

```json
{
  "message": "Config change not found for id: 00000000-0000-0000-0000-000000000000",
  "timestamp": "2026-03-22T12:00:00.123456789Z"
}
```

Typical mappings:

| Case | HTTP status |
|------|-------------|
| Bean validation failure on the request body | 400 |
| Business rule violation (`InvalidConfigChangeException`) | 400 |
| Unknown id (`ConfigChangeNotFoundException`) | 404 |
| Other unhandled errors | 500 with a generic message |

**Edge case validation** — Beyond bean validation, the service returns **400** with a clear `message` for rules such as: inappropriate `oldValue` / `newValue` for `ADD` or `DELETE`, equal old/new on `UPDATE`, and invalid list windows when **`from` is after `to`** (with both query parameters present).

---

## Design Decisions

- **In-memory repository** — Keeps the assignment runnable without database setup while still separating persistence behind `ConfigChangeRepository` and an `InMemoryConfigChangeRepository` implementation.
- **Layered architecture** — **Controller** handles HTTP; **service** owns use cases, business validation, and filtering for list; **repository** exposes `save`, `findById`, and `findAll` only.
- **Validation split** — **Jakarta Bean Validation** on `CreateConfigChangeRequest` enforces required fields and non-blank strings at the API edge. **Service-layer rules** enforce behaviour that depends on `ChangeType` (ADD / UPDATE / DELETE).
- **Monitoring notifier abstraction** — `MonitoringNotifier` is an interface; the default `LoggingMonitoringNotifier` logs at INFO for critical changes (with **Spring Retry** and a **`@Recover`** path on repeated failure) so the service stays testable and a real integration can be plugged in later.

---

## Trade-offs / Limitations

- **No persistence** — data is lost on restart.
- **No authentication or authorization**.
- **No pagination** — list returns all matching rows in memory.
- **No retry or circuit breaker** for the monitoring integration (logging only).

---

## Future Improvements

- **Database** (e.g. PostgreSQL) with migrations and a durable repository implementation.
- **Micrometer / Actuator** — richer JVM and HTTP metrics (beyond the custom `/metrics` JSON), dashboards, and SLO-aligned indicators.
- **Circuit breaker** and **bulkhead** patterns for real monitoring APIs, complementing retry.
- **Distributed tracing** (e.g. W3C `traceparent`, OpenTelemetry) building on correlation IDs and structured logs.
- **Structured logging** — log patterns that include MDC (e.g. `correlationId`) consistently across libraries.
- **Pagination and sorting** for list endpoints.

---

## Tests

```bash
./mvnw test
```

Includes unit tests for the service (mocked repository and notifier) and Spring **MockMvc** integration tests for the REST API.
