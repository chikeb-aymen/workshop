# After: Transactional Outbox — Three Independent Services

## What changed

The `rabbitTemplate.convertAndSend()` call has been removed from the HTTP request path entirely.
`OrderService` now writes two rows in one database transaction — `Order` + `OutboxEvent` — and returns.
A `@Scheduled` relay picks up the outbox row and publishes it to the broker as a separate, retryable operation.

```
BEFORE                                    AFTER

OrderService                              OrderService
  │                                         │
  ├─ orderRepository.save(order)            ├─ orderRepository.save(order)
  │                                         ├─ outboxRepo.save(outboxEvent)   ← same @Transactional
  └─ [listener fires after commit]          └─ return                         ← no broker call yet
       └─ rabbitTemplate.send(event)
            ↑ JVM crash window              OutboxRelay  (@Scheduled every 500 ms)
            ↑ broker-down = lost event         └─ outboxRepo.findPending()
                                               └─ rabbitTemplate.send(event)  ← retryable
                                               └─ outboxRepo.delete(row)      ← confirmed delivery
```

---

## The key insight

`OrderService` has **zero AMQP imports**. It does not know RabbitMQ exists. Its `@Transactional` boundary
covers exactly the business entities it owns. The broker is the relay's concern, not the service's.

---

## What each service does

### order-service (producer + outbox owner)
- Persists `Order` entities. The only writer.
- Exposes `GET /api/orders/{id}` — authoritative read endpoint.
- On each `placeOrder()`: writes `Order` + `OutboxEvent` in a **single `@Transactional`** call.
- `OutboxRelay` runs every 500 ms, reads `PENDING` rows ordered by `created_at`, publishes to `orders.exchange`, deletes on success.
- After 5 failed attempts, a row moves to `outbox_dead_letter`. The relay continues with the next row.
- `outbox.pending_count` Micrometer gauge is exposed for alerting.

### inventory-service (consumer A)
- Binds queue `inventory-service.order.placed` to `orders.exchange` with routing key `order.placed`.
- On each event: schema check → idempotency check → reserve stock → mark processed (one `@Transactional`).
- Has its own DLQ: `inventory-service.order.placed.dlq`.

### fulfillment-service (consumer B)
- Binds **its own** queue `fulfillment-service.order.placed` to the **same** exchange.
- Processes the same event independently — creates a packing job.
- Stop `inventory-service` → `fulfillment-service` is completely unaffected.

---

## Tracing a single request end-to-end

```
Client:              POST /api/orders

order-service:       1. BEGIN TRANSACTION
                     2. INSERT INTO orders ...
                     3. INSERT INTO outbox_events (status=PENDING, payload=OrderPlacedEvent JSON)
                     4. COMMIT                          ← both rows committed atomically
                     5. Return 201 Created              ← no broker call yet

OutboxRelay:         6. SELECT * FROM outbox_events WHERE status='PENDING' ORDER BY created_at
                     7. rabbitTemplate.send("orders.exchange", "order.placed", event)
                     8. DELETE FROM outbox_events WHERE id=...   ← confirmed delivery

inventory-service:   9.  Receive from queue "inventory-service.order.placed"
                     10. schemaVersion check (reject unknown → DLQ)
                     11. processedEventRepo.existsByEventId(eventId) → false
                     12. INSERT INTO stock_reservations (orderId, skuId, quantity)
                     13. INSERT INTO processed_events (eventId)     ← same @Transactional
                     14. COMMIT

fulfillment-service: 15. Receive same event from "fulfillment-service.order.placed"
                     16. Same idempotency pattern → INSERT INTO packing_jobs
```

**If the JVM crashes after step 4 and before step 7:** the `outbox_events` row survives in the DB.
The relay picks it up on restart. The order is never silently lost.

**If the relay publishes (step 7) but crashes before deleting (step 8):** the event is re-delivered.
Consumers detect the duplicate via `processed_events` and skip silently.

---

## How each before-problem is resolved

| Problem in `before/`                               | Resolved by                                                                    | File                                        |
|----------------------------------------------------|--------------------------------------------------------------------------------|---------------------------------------------|
| Silent event loss on JVM kill / rolling deployment | Outbox row survives restarts; relay picks it up                                | `OrderService.java`, `OutboxRelay.java`     |
| Broker failure = permanent event loss              | Relay retries until broker confirms; rows accumulate and drain on recovery     | `OutboxPublisher.java`                      |
| No audit trail                                     | `outbox_events` rows persist with `attempts`, `lastError`, `createdAt`         | `OutboxEvent.java`                          |
| No idempotency                                     | `processed_events` table keyed by `eventId` checked before acting              | `OrderPlacedConsumer.java` (both services)  |
| Exception leakage to HTTP caller                   | Broker never called inside the user request                                    | `OrderService.java` — zero AMQP imports     |
| Poison messages blocking relay                     | After 5 failures → `outbox_dead_letter`; relay continues with the next row     | `OutboxPublisher.java`                      |
| No observability                                   | `outbox.pending_count` Micrometer gauge wired at relay construction            | `OutboxRelay.java`                          |
| Schema changes breaking consumers silently         | `schemaVersion` field on every event; consumers reject unknown versions to DLQ | `OrderPlacedConsumer.java` (both services)  |

---

## How to run

```bash
cd after/
docker-compose up -d

# RabbitMQ management UI:    http://localhost:15672  (guest / guest)
# order-service:             http://localhost:8081
# inventory-service:         http://localhost:8082
# fulfillment-service:       http://localhost:8083
```

Place an order:
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-1","items":[{"skuId":"SKU-42","quantity":2,"unitPrice":29.99}]}'
```

Check the pending outbox count:
```bash
curl http://localhost:8081/actuator/metrics/outbox.pending_count
```

---

## Experiments to run

**Experiment 1 — Broker outage tolerance**
```bash
docker-compose stop rabbitmq
# Place 10 orders — all save to DB, outbox rows accumulate
curl http://localhost:8081/actuator/metrics/outbox.pending_count   # count > 0
docker-compose start rabbitmq
# Relay drains the backlog automatically within 500 ms
curl http://localhost:8081/actuator/metrics/outbox.pending_count   # count = 0
```

**Experiment 2 — Idempotency proof**
```bash
# Place one order, note the eventId in inventory-service logs
# Re-publish the same message via the RabbitMQ management UI
# Confirm: stock_reservations has exactly one row, processed_events has exactly one row
```

**Experiment 3 — Dead letter routing**
```bash
# Stop inventory-service
# Place an order — relay will retry 5 times then move to outbox_dead_letter
# SELECT * FROM outbox_dead_letter;  → inspect the failed row with lastError
# Start inventory-service — it will not receive the event (already in dead letter)
```

**Experiment 4 — Consumer isolation**
```bash
docker-compose stop inventory-service
# Place several orders — fulfillment-service processes them normally
docker-compose start inventory-service
# inventory-service consumes the queued backlog independently
```

**Experiment 5 — Add a third consumer without touching order-service**
```bash
# Copy fulfillment-service to fraud-detection-service
# Change queue to "fraud-service.order.placed", same routing key "order.placed"
# Start it — order-service is unmodified, unaware, and unaffected
```
