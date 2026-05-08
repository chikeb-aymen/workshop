# Transactional Outbox

###### Category: Architectures / Reliability / Data Consistency

## 1. Intent

Guarantee that a domain event is **always published** to a message broker after a successful database write,
and **never published** if that write is rolled back without using distributed transactions.
The mechanism: write the event into an `outbox_events` table inside the same local transaction as the business
entity, then have a separate relay process read and forward those records to the broker asynchronously.

---

## 2. The Real Pain

Your `OrderService.placeOrder()` saves a new `Order` to PostgreSQL and then publishes an `OrderPlaced` event
to RabbitMQ. In testing this is seamless. In production, two silent failure modes corrupt your data:

- **Silent event loss.** The JVM is OOM-killed, the pod is replaced by a rolling deployment, or RabbitMQ has a
  4-minute network partition at the exact moment after the database commits but before the message is sent.
  The order exists in the database. `InventoryService` and `FulfillmentService` never receive the event.
  Stock is never reserved. The order is never packed. No error is logged, the failure is invisible.
  Manual reconciliation takes a full day.
- **Ghost events.** `rabbitTemplate.convertAndSend()` executes inside the `@Transactional` method before the
  commit. RabbitMQ receives the event. Then a database constraint violation triggers a rollback. Consumers now
  process an event for an order that does not exist. `InventoryService` decrements stock for a phantom order.
  Warehouse staff scan a barcode that returns "order not found".

Neither failure is rare. Rolling deployments, broker blips, GC pauses, and long network timeouts trigger them
regularly at scale. You cannot fix them by reordering lines of code inside the same method.

---

## 3. Core Idea (in one breath)

Write the event as a row in an `outbox_events` table **in the same database transaction** as your business
write, then relay it to the broker as a separate, retryable, observable background operation.

---

## 4. Key Components

| Component             | Role                                                                                                                                                                        |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Business Entity**   | The domain object being written (e.g. `Order`). Lives in the main application tables.                                                                                       |
| **Outbox Table**      | `outbox_events` in the **same** database. Stores serialized events not yet relayed. Written atomically with the business entity in the same transaction.                    |
| **Outbox Relay**      | A background process (scheduled poller or CDC connector) that reads unprocessed rows, publishes them to the broker, and **deletes or marks them after confirmed delivery.** |
| **Message Broker**    | RabbitMQ, Kafka, SNS. Receives events from the relay → never from inside the application transaction.                                                                       |
| **Event ID**          | A UUID generated at write time, stored in the outbox row and carried in the published message. Consumers use it to guard against duplicate processing.                      |
| **Dead Letter Table** | Rows that fail after N relay attempts are moved here instead of blocking the relay forever.                                                                                 |

---

## 5. Applicability: when to reach for it

**Use when:**
- You must guarantee that a business write and an event publication succeed or fail atomically.
- You cannot afford silent event loss: financial transactions, inventory changes, order placement, billing.
- The broker is occasionally unavailable, and you need events to drain automatically on recovery.
- You are building on top of an Event Notification system and need the publishing step to be durable.

**Avoid or complement when:**
- You are already using CDC (Debezium) on the business table itself, the WAL log acts as the outbox; a
  separate outbox table may be redundant.
- Kafka transactional producers fit your stack and your team is comfortable with their operational complexity.
- Data loss is explicitly acceptable and consumers are idempotent, a fire-and-forget publish may suffice.

---

## 6. Consequences

### Benefits
- **Atomic correctness.** Event publication is causally tied to the DB write via a local transaction.
  No distributed commit protocol required.
- **At-least-once delivery guaranteed.** The relay retries until the broker confirms receipt.
  Events are never silently dropped.
- **Broker outage tolerance.** Events accumulate in the outbox during a broker outage and drain when it recovers.
- **Audit trail by design.** The outbox table is a durable record of every committed event: when it was
  written, how many relay attempts, and whether it was delivered.
- **Clean application code.** `OrderService` writes to two DB tables. Zero broker dependency in the
  transaction path.

### Liabilities / sharp edges
###### (take in consideration)
- **At-least-once, not exactly-once.** The relay may publish a duplicate if it crashes after publishing but
  before marking the row as sent. Consumers must be idempotent this is non-negotiable.
- **Polling latency.** A scheduled poller introduces end-to-end delay equal to its interval (100 ms–5 s
  typically). Use CDC (Debezium) for sub-second latency requirements.
- **Outbox table growth.** Processed rows must be cleaned up. An unbounded `outbox_events` table is a
  production incident. Implement scheduled deletion or archival from day one.
- **Relay is a new operational concern.** Monitor its lag. Alert on backlog growth. Handle relay failures
  independently of the application.
- **Schema coupling.** The serialized event in the outbox row is a contract. Schema evolution must be
  versioned, same as any event schema.

---

## 7. Principles that every Transactional Outbox implementation must resolve

These are not optional extras. Each represents a failure mode you will hit in production.

| # | Principle                           | What goes wrong without it                                                          | How it is resolved in `after/`                                                                                      |
|---|-------------------------------------|-------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| 1 | **Single local transaction**        | DB and broker can diverge; events are lost or duplicated at write time              | `Order` + `OutboxEvent` are written in one `@Transactional` call. Broker is never touched inside the transaction.   |
| 2 | **Relay is outside the write path** | A broker outage blocks the user request; exceptions surface as HTTP 500s            | A `@Scheduled` relay runs in a separate thread. Broker errors never propagate to the HTTP caller.                   |
| 3 | **Idempotent consumers**            | Relay may publish twice; consumers double-process charges, emails, stock decrements | Consumers check `processed_events` keyed by `event_id` before acting, inside a transaction.                         |
| 4 | **Outbox cleanup**                  | Table grows unbounded; DB I/O degrades; disk exhaustion                             | Relay deletes rows after confirmed delivery. A separate job archives rows older than N days.                        |
| 5 | **Relay lag observability**         | Silent backlog grows for hours; downstream services are permanently behind          | Relay exposes `outbox.pending_count` metric. Alert fires when count > threshold for > 2 minutes.                    |
| 6 | **Ordering per aggregate**          | Two updates to the same order publish out of order; consumers apply stale state     | Relay publishes in `created_at` insertion order. Events carry `aggregate_version` for staleness detection.          |
| 7 | **Schema versioning**               | Producer adds/renames a field; consumer silently misprocesses or throws NPE         | `schema_version` field on every event. Consumer rejects unknown versions to DLQ rather than silently misprocessing. |
| 8 | **Poison message handling**         | A malformed outbox row blocks the relay loop; all subsequent events are stuck       | After N failed publish attempts, the row is moved to `outbox_dead_letter`. The relay continues with the next row.   |

---

## 8. Mapping to this workshop

| Folder                       | What to study                                                                                    |
|------------------------------|--------------------------------------------------------------------------------------------------|
| `before/`                    | `OrderService` with the dual-write bug, both failure modes annotated in the code.                |
| `after/`                     | Outbox write, relay poller, idempotent consumers, dead letter table.                             |
| `after/docker-compose.yml`   | Runs PostgreSQL + RabbitMQ + all services. Start here.                                           |
| `after/order-service/`       | Writes `Order` + `OutboxEvent` in one transaction. Zero broker calls in the transaction path.    |
| `after/relay/`               | Polls `outbox_events`, publishes to RabbitMQ, marks rows as sent, moves failures to dead letter. |
| `after/inventory-service/`   | Idempotent consumer with `processed_events` guard and version check.                             |
| `after/fulfillment-service/` | Second independent consumer — same pattern, different business logic.                            |

---

## 9. Where you meet Transactional Outbox in the wild

- **Stripe.** Every charge and payout writes to an internal outbox before being fanned out to webhooks and
  internal event pipelines. No payment event is ever silently lost.
- **Debezium + Kafka Connect.** The canonical CDC-based outbox: the application writes to `outbox_events`,
  Debezium captures the insert via the PostgreSQL WAL and routes it to Kafka topics. Sub-second latency,
  zero polling overhead.
- **AWS DynamoDB Streams.** Every DynamoDB write streams to a Lambda via DynamoDB Streams, which publishes
  to SNS/SQS. The stream acts as the outbox; Lambda is the relay.
- **Axon Framework.** Built-in outbox via `EventStorageEngine`: domain events are written to the event store
  in the same transaction as aggregate state, then forwarded to the event bus by the framework's publishing
  mechanism.
- **Any banking or payments system.** Dual-write is a compliance risk in finance. The outbox (or event
  sourcing) is mandatory architecture wherever "the event was emitted" must be provable.

---

## 10. Problem statement

See **`problem.md`** for the workshop scenario. Read it before diving into `before/` and `after/`.