# Ticket Sale API

A Spring Boot backend for a high-demand ticket sale: thousands of fans click "buy" at the same
moment, and the system must **never sell more tickets than exist**.

Buying works in two steps, like real ticket sites: a buyer **holds** tickets for 5 minutes, then
**pays**. Holds that aren't paid in time are released and the tickets go back on sale.

## Tech stack

- Java 25, Spring Boot 4
- PostgreSQL 17 (Docker), Flyway migrations
- Spring JDBC (`JdbcTemplate`, plain SQL)
- JUnit 5, Mockito, AssertJ, Testcontainers

## How overselling is prevented

The obvious approach — read the count, subtract, write it back — breaks under load: two buyers
read the same number and both get a ticket. An early in-memory version sold **29 tickets for a
10-ticket show** when 2,000 buyers arrived at once.

A Java lock (`synchronized`) fixes it on one server, but not when several servers share the database.

The fix used here is a single atomic SQL statement:

```sql
UPDATE events SET tickets_left = tickets_left - ?
WHERE id = ? AND tickets_left >= ?
```

The check and the decrement happen in one step inside Postgres, which locks the row for only a
few milliseconds. It returns `1` if the tickets were taken and `0` if not, so there is no window
for two buyers to take the same ticket — no matter how many app servers are running.
A `CHECK (tickets_left >= 0)` constraint is an extra safety net.

## Seat holds

1. `POST /events/{id}/hold` takes the tickets immediately (the atomic `UPDATE` above) and saves a
   hold that expires in 5 minutes. Both happen in one transaction.
2. `POST /holds/{holdId}/pay` marks the hold as paid — only if it is still `HELD` and not expired.
3. A scheduled job runs every 30 seconds and, in one SQL statement, marks expired holds as
   `EXPIRED` and adds their tickets back.

No lock is held during the 5 minutes; the hold is just a row with an expiry time. Because every
step checks `status = 'HELD'`, a payment and an expiry can't both win, and a hold can't be
released twice.

## API

| Request | Response |
|---|---|
| `GET /events` | `200` list of events |
| `GET /events/{id}` | `200 {id, name, ticketsLeft}` · `404` unknown event |
| `POST /events/{id}/hold` body `{"quantity": 3}` (optional, default 1) | `200 {id, quantity, expiresAt}` |
| | `409` not enough tickets (nothing is taken) · `400` quantity not 1–6 · `404` unknown event |
| `POST /holds/{holdId}/pay` | `200` paid · `409` hold expired or not found |

## Running it

Requirements: JDK 25 and Docker Desktop.

```bash
docker compose up -d        # Postgres on port 55432
./mvnw spring-boot:run      # app on http://localhost:8080
```

Flyway creates the tables and two sample events on startup.

```bash
curl http://localhost:8080/events
curl -X POST http://localhost:8080/events/1/hold -H "Content-Type: application/json" -d "{\"quantity\": 2}"
```

`BuyerSimulator` (a separate `main` class) fires 2,000 parallel hold requests at a running app.

## Tests

```bash
./mvnw test                 # Docker must be running
```

Tests that need a database use **Testcontainers**: each run starts a throwaway Postgres in Docker,
Flyway applies the same migrations, and the container is deleted afterwards.

| Test | What it proves |
|---|---|
| `TicketConcurrencyTest` | 2,000 concurrent buyers, 10 tickets → exactly 10 sold, 0 left |
| `HoldRepositoryRealTest` | the SQL: all-or-nothing `take`, expired holds give tickets back |
| `HoldServiceRealTest` / `HoldControllerRealTest` | the whole flow against a real database |
| `HoldServiceMockTest` / `HoldControllerMockTest` | business logic and HTTP status codes in isolation (Mockito) |

## Project structure

```
src/main/java/com/example/tickets/
├── event/   EventController, EventService, EventRepository, Event
└── hold/    HoldController, HoldService, HoldRepository, HoldExpiryJob, Hold
src/main/resources/db/migration/   Flyway migrations V1–V3
```

Each feature follows Controller → Service → Repository.

## Roadmap

- [x] Atomic ticket purchase with Postgres
- [x] Seat holds with expiry, multiple tickets per hold, several events
- [x] Unit, integration and concurrency tests with Testcontainers
- [ ] GitHub Actions CI
- [ ] Kafka: purchases as events, dead-letter queue, monitoring with Prometheus + Grafana
