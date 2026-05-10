# Before: Dual-Write Order Service

## What you are looking at

`OrderServiceApplication` is a single Spring Boot application. When a customer places an order,
`OrderService.placeOrder()` saves the `Order` to PostgreSQL, then fires a Spring `ApplicationEvent`.
A `@TransactionalEventListener(phase = AFTER_COMMIT)` relay picks it up and calls
`rabbitTemplate.convertAndSend(...)` to publish the `OrderPlaced` event to RabbitMQ.

Start here: open `service/OrderService.java` and `messaging/OrderEventRelay.java`.
Every failure mode is annotated in the code.

---

## Smells to notice

### 1. The AFTER_COMMIT listener looks correct — but it is not

`@TransactionalEventListener(phase = AFTER_COMMIT)` fires after the DB transaction commits. This avoids
ghost events (events for rolled-back orders). But there is still a gap:

[DB commits] →  gap →  [AFTER_COMMIT listener fires] → rabbitTemplate.send()

In that gap the JVM can be OOM-killed, the pod can be replaced by a rolling deployment, or the OS can
terminate the process. When the service restarts, the order is in the DB. The listener never ran.
The event is gone permanently — there is no record it was supposed to be emitted.

### 2. Broker failure = permanent event loss

`rabbitTemplate.convertAndSend()` is called outside the transaction boundary (AFTER_COMMIT). If RabbitMQ
is down, the call throws a `AmqpException`. At this point the DB transaction is already committed.
There is nothing to retry. The event is silently discarded.
The only mitigation available here is logging — you cannot roll back a committed transaction.

### 3. No record of what was supposed to be emitted

There is no durable store of "event X was committed and needs to be delivered." If the service crashes after
commit but before the listener fires, or if the listener fails to publish, the event is gone without trace.
Support engineers have no way to verify whether an event was ever emitted for a given order.

### 4. Broker unavailability surfaces to the caller

When `rabbitTemplate.convertAndSend()` throws, the exception propagates back to the
`@TransactionalEventListener`. Spring swallows it by default, but with `AmqpException` it can surface up
the call stack and cause the response to the original HTTP request to fail even though the order was
already committed successfully.

### 5. No idempotency anywhere

There is no `event_id` guard. If a consumer receives the same event twice (broker re-delivery, client
retry), it will process it twice: two stock reservations, two packing jobs, two confirmation emails.

---

## What to compare

After studying these problems, open `after/` and trace:

1. How `OrderService` writes to the `outbox_events` table instead of calling RabbitMQ.
2. How the relay handles broker outages without any impact on the user-facing write path.
3. How every problem above is resolved by a specific design decision in the after code.

The goal is not just to see "it works differently", it is to name **which file and which line** fixes
each specific problem you found here.
