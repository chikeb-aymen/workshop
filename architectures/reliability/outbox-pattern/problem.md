# Problem: Order Platform — Dual-Write Consistency

###### (👀 don't cheat if you want to learn), take your time to think.

You are on the **orders team** at an e-commerce company. The core service is `OrderService`, which handles
order placement. When a customer places an order:

1. The `Order` is persisted to PostgreSQL.
2. An `OrderPlaced` event must reach two downstream services:
  - **InventoryService**: reserves the items in the order so they are not oversold.
  - **FulfillmentService**: creates a packing job and assigns a warehouse slot.

---

## Current state (the `before/` problem)

`OrderService.placeOrder()` saves the order, then fires a Spring `ApplicationEvent`. A
`@TransactionalEventListener(phase = AFTER_COMMIT)` relay picks it up and calls
`rabbitTemplate.convertAndSend(...)`.

These looks correct the event only fires after the DB commits, so no ghost events. With **2 000 orders per
hour**, you are seeing:

- **Silent inventory drift (weekly):** Ops reports the inventory count in `InventoryService` is consistently
  lower than the number of orders in the DB. After a week of investigation, engineering finds the root cause:
  Kubernetes rolling deployments. When the `order-service` pod is replaced mid-request, the DB transaction
  commits (the order is saved) but the pod is killed before the `@TransactionalEventListener` fires. The event
  is lost. `InventoryService` never reserves the stock. Items oversell during every deployment window.
- **Ghost fulfillment jobs (twice this month):** A flash sale creates a race condition: a database constraint
  violation fires after `rabbitTemplate.convertAndSend()` executes but before the transaction commits (in the
  other failure path where the publish happens inside the transaction in a different service). `FulfillmentService`
  creates a packing job for an order that does not exist. Warehouse staff scan a barcode that returns
  "order not found". Root cause takes 3 hours to diagnose.
- **RabbitMQ outage = permanently lost events:** RabbitMQ had a 4-minute network partition last Tuesday.
  During that window, 140 orders were placed. The `@TransactionalEventListener` fired for each one, but
  `rabbitTemplate.convertAndSend()` threw a connection exception. The transaction was already committed.
  There was nothing to retry. Those 140 events are gone permanently. `InventoryService` and
  `FulfillmentService` are still out of sync. Manual reconciliation took a full day.
- **No audit trail:** When a customer calls support ("my order was placed but never shipped"), engineering
  has no record of whether the event was ever emitted. They must manually query the DB, check RabbitMQ
  dead letters, and trace logs across three services.
- **Broker unavailability surfaces as HTTP 500s:** When RabbitMQ is flapping, some
  `rabbitTemplate.convertAndSend()` calls throw immediately. The exception propagates up through the
  `@TransactionalEventListener`, surfaces in the caller's thread, and returns a 500 to the customer.
  Customers retry, creating duplicate orders.

---

## Your design constraints

- **The DB write and the event must succeed or fail together.** An order in the DB with no event, or an
  event for a rolled-back order, are both unacceptable.
- **A broker outage must not fail order placement.** If RabbitMQ is down for 10 minutes, customers can
  still place orders. Events drain automatically when the broker recovers.
- **No distributed transactions.** 2PC across PostgreSQL and RabbitMQ is off the table.
- **At-least-once delivery is acceptable.** Consumers must handle duplicate delivery.
- **The relay must be observable.** Ops must see the pending event count at a glance and be alerted when
  the backlog grows.
- **Adding a `FraudDetectionService` consumer must require zero changes to `OrderService`.**

---

## What you deliver in this workshop

Design and implement a Transactional Outbox system that:

1. Writes the `Order` and an `OutboxEvent` row in a single local database transaction.
   No broker call anywhere inside the transaction.
2. A relay (scheduled poller) reads unprocessed outbox rows, publishes them to RabbitMQ, and marks them
   as sent after confirmed delivery.
3. The relay retries failed publishes with exponential backoff. After 5 failures, the row moves to an
   `outbox_dead_letter` table and the relay continues without blocking.
4. `InventoryService` and `FulfillmentService` are idempotent: re-delivery of the same `event_id` has no
   additional effect.
5. The relay exposes a `outbox.pending_count` metric. A Prometheus alert fires when `pending_count > 100`
   for more than 2 minutes.
6. Adding a `FraudDetectionService` consumer requires zero changes to `OrderService` or the outbox relay.

Study `before/` to understand the failure modes, then `after/` to see each design decision that resolves them.
Map every problem in `before/README.md` to the exact file and line in `after/` that fixes it.