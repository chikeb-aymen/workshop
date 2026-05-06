# Event Notification

###### Category: Architectures / Event-Driven

## 1. Intent

Decouple the producer from its consumers by emitting a **minimal notification event** whenever something changes,
instead of calling each downstream system directly. The event signals *"entity X changed"*; any interested service
subscribes, receives the signal, and fetches the details it needs from the producer's authoritative API.
The producer never imports, constructs, or calls any consumer, they become independent services that react on their
own time.

---

## 2. The Real Pain

Your `UserService` updates a user profile and then synchronously calls `EmailService`, `AnalyticsService`, and
`NotificationService` in sequence , all inside the same database transaction. This model breaks in production in
multiple compounding ways:

- **Cascading failures.** If `EmailService` responds with a 500, Spring rolls back the database transaction. The user's
  profile is not saved , not because the update was wrong, but because an unrelated downstream service happened to fail
  at the wrong moment.
- **Tight coupling.** `UserService` must import `EmailClient`, `AnalyticsClient`, `NotificationClient`. Every new
  consumer requires opening `UserService`, adding a new dependency, and redeploying. The class never stops growing.
- **Latency multiplication.** The user waits for every call to finish in sequence. Email takes 2 s, analytics 3 s,
  notification 1 s → the user waits at least 6 s for a profile update. None of those downstream operations were part of
  the user's request in the first place.
- **No idempotency.** If the client retries the HTTP call (common on timeout), every retry triggers a duplicate email,
  a duplicate analytics event, a duplicate notification. There is no guard anywhere.
- **PII leakage.** The full `User` object , phone number, home address, email , is serialized and sent over the network
  to every downstream service, even those that only need a display name.
- **Thundering herd.** A burst of profile updates fans out to all three services simultaneously, amplifying load at the
  worst possible time.
- **No ordering control.** Two concurrent updates for the same user can interleave. No version tracking exists;
  downstream services cannot tell which update is newer.

---

## 3. Core Idea (in one breath)

Emit a small *"something changed"* event carrying only the entity's identifier; let interested services subscribe
independently and fetch what they need from the single source of truth.

---

## 4. Key Components

| Component | Role |
|---|---|
| **Producer** | Owns the entity. Saves the change to its own DB. Emits a minimal event to the message broker. Never calls any consumer. |
| **Event** | A factual statement about the past: `UserProfileChanged`. Carries `eventId`, `entityId`, `changeType`, `entityVersion`, `occurredAt`, `correlationId`, `schemaVersion`. No PII, no full object. |
| **Message Broker** | Transports the event from producer to all consumers. Here: RabbitMQ topic exchange `user.events`. Provides durability, buffering, and per-consumer independent queues. |
| **Consumer** | Subscribes to its own queue. Receives the event, guards idempotency, fetches entity details from the producer's API, and performs its own business logic. |
| **Source-of-Truth API** | The producer's read endpoint (`GET /api/users/{id}`). Consumers call this after receiving an event to get current, authoritative data. |
| **Dead Letter Queue (DLQ)** | A per-consumer holding queue for events that exhausted all retry attempts. Prevents poison messages from blocking the main queue forever. |
| **Idempotency Store** | A `processed_events` table per consumer, keyed by `eventId`. Guards against duplicate processing when the broker re-delivers. |

---

## 5. Applicability: when to reach for it

**Use when:**
- You need to notify multiple independent services about a change without coupling the producer to each of them.
- The data is sensitive or large, and you do not want to embed it in the event (minimal-payload is a design goal).
- Consumers can tolerate *latest-state semantics*: they fetch current state when they process the event, not the state
  at the exact moment the event was emitted.
- You want a new consumer to subscribe without any change to the producer.
- You are starting an event-driven system and need the simplest, lowest-risk entry point.

**Avoid or complement when:**
- Every consumer *always* needs the full data immediately and you cannot afford the extra fetch call → consider
  **Event-Carried State Transfer** (embed the data in the event).
- You need to reconstruct exactly what state existed *at the time of the event* → consider **Event Sourcing**.
- The consumer must react to the *difference* (diff) between old and new state, not just the new state → include a
  before/after snapshot in the event.
- High-throughput fanout to many slow consumers → put a queue with backpressure in front of the fetch calls.

---

## 6. Consequences

### Benefits
- **True decoupling.** The producer has zero dependencies on consumers. Adding, removing, or replacing a consumer
  requires zero changes to the producer.
- **Temporal decoupling.** Consumers do not need to be running when the event is emitted. The broker holds events;
  consumers catch up when they restart.
- **Independent failure isolation.** A consumer failure never propagates back to the producer or to other consumers.
  Each consumer has its own queue, its own retry policy, and its own DLQ.
- **PII minimization by design.** Sensitive data stays in the producer's store; only opaque identifiers travel on the
  event bus.
- **Independent scaling.** Each consumer scales according to its own load without affecting any other service.
- **Audit trail.** Every change is signalled as an event; `eventId`, `occurredAt`, and `correlationId` give you a
  natural audit log of what changed and when.

### Liabilities / sharp edges
###### (take in consideration)
- **Extra network hop.** Consumers make a fetch call after receiving the event. If the producer's API is slow or down,
  consumers must retry. This is the fundamental trade-off of the minimal-payload approach.
- **Latest-state semantics only.** The consumer fetches the *current* state, not the state at the moment of the event.
  If the entity changed again between event emission and fetch, the consumer sees the newer state. This is usually
  acceptable but must be understood and designed for.
- **At-least-once delivery.** RabbitMQ (and most brokers) can re-deliver a message after a crash, network partition,
  or consumer restart. Idempotency guards are not optional , they are mandatory.
- **Ordering is not guaranteed.** Events for the same entity can arrive out of order across restarts or re-queues.
  The `entityVersion` field in the event lets consumers detect and handle stale deliveries; it does not enforce strict
  ordering by itself.
- **Callback storm risk.** Many consumers each fetching at the same time can overload the producer's read API during
  a burst. Mitigate with: consumer-side backoff, prefetch limiting, caching, circuit breakers.
- **Schema evolution requires discipline.** Even a minimal event has a shape. Changing field names or types without
  a version strategy breaks consumers. The `schemaVersion` field and a backward-compatible evolution policy are
  required from day one.

---

## 7. Principles that every Event Notification implementation must resolve

These are not optional extras. Each represents a failure mode you will hit in production.

| # | Principle                                     | What goes wrong without it                                                                                        | How it is resolved in `after/`                                                                                                                       |
|---|-----------------------------------------------|-------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | **Clear ownership and source of truth**       | Multiple services hold copies; they diverge; no one knows which is right                                          | Only `UserService` writes users. Consumers call `GET /api/users/{id}` , one authoritative answer                                                     |
| 2 | **Minimal payload: signal only**              | PII and large objects travel on the bus, spread across services, and get retained in broker logs                  | Event carries `eventId`, `entityId`, metadata , nothing else. No phone, no address, no email                                                         |
| 3 | **Idempotency**                               | Broker re-delivers → duplicate emails, duplicate analytics rows, duplicate charges                                | `processed_events` table per consumer, checked before and inserted after in the same `@Transactional` boundary                                       |
| 4 | **Ordering is not guaranteed: design for it** | Out-of-order delivery causes consumers to apply a stale update over a newer one                                   | `entityVersion` (JPA `@Version`) in the event. Consumers can detect and discard stale events                                                         |
| 5 | **Resilience: retries and DLQ**               | Transient failures silently drop events or block the queue forever                                                | `@Retryable` with exponential backoff + jitter. After exhausting attempts, event routes to DLQ. `setDefaultRequeueRejected(false)`                   |
| 6 | **Avoid callback storms**                     | Burst of events → all consumers fetch simultaneously → producer overloaded                                        | `prefetchCount` limits concurrent fetch calls per consumer. Add circuit breaker + short TTL cache for production                                     |
| 7 | **Schema evolution**                          | Producer adds a field → consumer silently ignores it, or removes a field → consumer throws NullPointerException   | `schemaVersion` field on every event. Consumer rejects unknown versions to DLQ for inspection rather than silently misprocessing                     |
| 8 | **Security and PII minimization**             | Event bus retains messages; if PII is in the event it is retained forever and visible to anyone with queue access | No PII in the event. `UserDto` on the read API deliberately omits sensitive fields. Enforce topic ACLs in production                                 |
| 9 | **Observability**                             | A request touches 3 services across 4 hops; logs are unrelated; root cause analysis takes hours                   | `correlationId` set in the HTTP request, carried in the event, placed in MDC on the consumer , every log line across all services shares the same ID |

---

## 8. Mapping to this workshop

| Folder                     | What to study                                                                                                              |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `before/`                  | Synchronous, coupled `UserService` that calls all consumers directly. Read `UserService.java` for annotated failure modes. |
| `after/`                   | Event-driven decomposition: `user-service` (producer), `email-service` (consumer A), `analytics-service` (consumer B).     |
| `after/docker-compose.yml` | Runs RabbitMQ + all three Spring Boot services. Start here to see the system live.                                         |
| `after/user-service/`      | Source of truth. Emits `UserProfileChangedEvent`. Exposes `GET /api/users/{id}` for fetch-back.                            |
| `after/email-service/`     | Consumer with idempotency, schema check, fetch-back, retry, DLQ.                                                           |
| `after/analytics-service/` | Second independent consumer , same pattern, different business logic.                                                      |

---

## 9. Where you meet Event Notification in the wild

- **GitHub Webhooks.** GitHub emits `push`, `pull_request`, `check_run` events carrying only repository coordinates and
  commit SHA. Your CI system receives the notification and fetches details (diff, files changed) via GitHub's REST API
  if needed.
- **Stripe / PayPal.** `payment.succeeded` carries a payment ID. Your platform receives it and calls
  `GET /v1/charges/{id}` to retrieve the full charge object.
- **Shopify.** `orders/created` carries an order ID. Your fulfilment service fetches the order via REST to decide
  routing and packing.
- **AWS S3 Event Notifications.** S3 emits `ObjectCreated` with bucket and key. Your Lambda or SQS consumer fetches
  the object to process it.
- **Kubernetes Informers.** The API server emits watch events (`ADDED`, `MODIFIED`, `DELETED`) carrying the object
  metadata. Controllers reconcile by fetching the current object state from the API.
- **Any SaaS with a "Webhooks" settings page.** The common pattern: event fires with an ID, your handler fetches
  details if it needs them.

---

## 10. Problem statement

See **`problem.md`** for the workshop scenario. Read it before diving into `before/` and `after/`.
