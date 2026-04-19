# Observer Pattern ,  Spring Boot example

###### Category: Behavioral Pattern

This module shows how the Observer idea maps to Spring: **`ApplicationEventPublisher`** acts like the subject (publish side), and **`@EventListener`** methods act like observers (subscribe side).

## Mapping to classic Observer

| Classic Observer | Spring |
|---|---|
| Subject | Your service + `ApplicationEventPublisher` |
| Observer interface | Optional; listeners consume typed events (`OrderPlacedEvent`) |
| `notify()` | `publishEvent(...)` |
| Concrete observers | `@Component` classes with `@EventListener` |

Spring discovers listener beans and invokes them when matching events are published, without the publisher listing each listener explicitly.

## Layout

- **`event/OrderPlacedEvent`** ,  domain event (extends `ApplicationEvent`).
- **`service/OrderService`** ,  places an order and publishes `OrderPlacedEvent`.
- **`listener/*`** ,  react to `OrderPlacedEvent` (email, inventory, warehouse, analytics).

## Run

From this directory (`spring-example`):

```bash
mvn spring-boot:run
```

## Try it

Place an order (default port **8080**):

```bash
curl -s -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"id":"ORD-001","product":"Laptop","quantity":1}'
```

The console should show `OrderService` plus each listener’s log line, similar to the plain Java `after/` example.

## Notes

- Listeners run **synchronously** on the publishing thread unless you enable async handling (see exercises).
- You can use **conditional** listeners (`condition` on `@EventListener`) or **ordering** (`@Order`) when you need stricter behavior.
