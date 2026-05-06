# After: Event Notification — Three Independent Services

## What changed

The single `UserPlatformApplication` has been split into three independent Spring Boot services connected through
RabbitMQ. `UserService` was refactored into a true domain service that owns its data and emits events. Email and
analytics work became separate, independent consumers.

```
BEFORE                                   AFTER

UserService ──► EmailClient              UserService ──► RabbitMQ exchange "user.events"
             ├──► AnalyticsClient                              │
             └──► NotificationClient                ┌──────────┴──────────┐
                                             email-service       analytics-service
                                               (queue A)           (queue B)
```

---

## The key insight

`UserDomainService.updateProfile()` calls `eventPublisher.publishUserProfileChanged()` — one line.
Everything else — who receives it, how many consumers there are, what they do with it — is the broker's and each
consumer's concern. `UserDomainService` has **zero imports** from email or analytics packages.

---

## What each service does

### user-service (producer + source of truth)
- Persists user entities. The only writer.
- Exposes `GET /api/users/{id}` — the authoritative read endpoint.
- On each profile update: saves, then emits `UserProfileChangedEvent` to exchange `user.events`.
- The event carries: `eventId`, `entityId` (userId), `changeType`, `entityVersion`, `correlationId`, `schemaVersion`.
- It carries **nothing else**. No phone, no address, no display name.

### email-service (consumer A)
- Binds queue `email-service.user.events` to exchange `user.events`.
- On each event:
  1. Rejects unknown `schemaVersion` → DLQ.
  2. Checks `processed_events` table → skip if already processed (idempotency).
  3. Calls `GET /api/users/{userId}` on user-service (fetch-back).
  4. Sends profile-changed email.
  5. Marks event as processed in the same `@Transactional` scope.
- Has its own DLQ: `email-service.user.events.dlq`.
- Has its own retry: 3 attempts, exponential backoff with jitter.

### analytics-service (consumer B)
- Binds **its own** queue `analytics-service.user.events` to the **same** exchange.
- Processes the **same** event independently — separate queue, separate DB, separate DLQ, separate retry.
- Records `(userId, changeType, entityVersion, occurredAt)` for the analytics pipeline.
- Completely unaware of email-service. Stop email-service → analytics-service unaffected.

---

## Tracing a single request end-to-end

```
Client:           PATCH /api/users/abc123
                  X-Correlation-Id: req-456

user-service:     1. Save user to DB (version → 5)
                  2. Publish to exchange "user.events":
                     { eventId: "evt-789", entityId: "abc123", entityVersion: 5,
                       changeType: "UPDATED", correlationId: "req-456", schemaVersion: 1 }
                  3. Return 200 OK immediately

email-service:    1. Receive event from queue
                  2. MDC.put("correlationId", "req-456")
                  3. Check processed_events → not found
                  4. GET /api/users/abc123 → { email: "...", displayName: "..." }
                  5. Send email
                  6. Insert processed_events(eventId="evt-789")

analytics-service: 1. Receive same event from its own queue
                   2. MDC.put("correlationId", "req-456")
                   3. Check its own processed_events → not found
                   4. Record analytics entry
                   5. Insert processed_events(eventId="evt-789")
```

The `correlationId: "req-456"` appears in **every log line** across all three services.

---

## How each before-problem is resolved

| Before problem | Resolved by | File |
|---|---|---|
| Tight coupling — UserService knows all consumers | Producer owns only the exchange. Each consumer owns its queue. | `user-service/config/RabbitMQConfig.java` |
| Cascading failure — email outage fails user save | Consumer failure never touches the DB transaction in user-service | `email-service/consumer/UserEventConsumer.java` |
| Latency multiplication — user waits for all calls | User-service returns after publish. Consumers process asynchronously | `UserDomainService.java`, `EventPublisher.java` |
| No idempotency — duplicate calls = duplicate work | `processed_events` table + idempotency check before processing | `email-service/idempotency/ProcessedEvent.java` |
| PII leakage — full user pushed everywhere | Event carries only `entityId`. Consumers fetch a scoped `UserDto` | `UserProfileChangedEvent.java`, `UserController.UserDto` |
| Thundering herd — load amplified immediately | `prefetchCount=1` limits concurrent fetch calls per consumer | `email-service/config/RabbitMQConfig.java` |
| No ordering control — concurrent updates race | `entityVersion` (JPA `@Version`) included in event | `User.java` (`@Version`), `UserProfileChangedEvent.java` |

---

## Running the system

```bash
cd after/
docker-compose up -d

# RabbitMQ management UI:  http://localhost:15672  (guest / guest)
# user-service:            http://localhost:8081
# email-service:           http://localhost:8082
# analytics-service:       http://localhost:8083

# Create a user
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","displayName":"Alice"}'

# Update the profile and watch all three service logs
curl -X PATCH http://localhost:8081/api/users/{id} \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: my-trace-001" \
  -d '{"displayName":"Alice Smith"}'
```

---

## Experiments to run

**Experiment 1 — Temporal decoupling (consumer isolation)**
```bash
docker-compose stop analytics-service
# Update a user profile multiple times — observe email-service works normally
docker-compose start analytics-service
# Watch analytics-service consume the queued backlog
```

**Experiment 2 — Idempotency proof**
```bash
# Send the same PATCH request twice rapidly (simulate client retry)
# Confirm: one email sent, one analytics entry, despite two HTTP calls
# Check processed_events table in each service's DB
```

**Experiment 3 — DLQ routing**
```bash
# Temporarily change schemaVersion in EventPublisher to 99
# Send a PATCH request
# Observe: event routes to DLQ, not retried infinitely
# Inspect in RabbitMQ UI: email-service.user.events.dlq
```

**Experiment 4 — Add a third consumer without touching user-service**
```bash
# Create sms-service: copy analytics-service, change queue name to
# "sms-service.user.events", bind same routing key
# Start it — user-service is unmodified, unaware, and unaffected
```

**Experiment 5 — Trace a correlationId across all logs**
```bash
docker-compose logs -f | grep "my-trace-001"
# See the same correlationId appear in user-service, email-service, analytics-service logs
```
