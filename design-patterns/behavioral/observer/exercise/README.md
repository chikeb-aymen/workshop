# Exercises — Observer Pattern

Work through these in order. Do **not** copy a full solution from the workshop text; implement and run the app yourself.

---

## 1. Trace the event path

Run the Spring `spring-example` app, call `POST /orders` once, and **list every class** involved from the HTTP request until all listener log lines appear. For each step, note whether it is “publish” or “subscribe” behavior in Observer terms.

**Goal:** You can explain Spring’s role as subject vs listeners as observers without opening this README.

---

## 2. Add a new observer without touching `OrderService`

Add a new `@Component` listener that reacts to `OrderPlacedEvent` and logs a fifth concern (for example: fraud check, SMS, or loyalty points).

**Constraints:**

- Do **not** modify `OrderService.publishEvent` logic beyond what is strictly necessary (ideally: zero changes there).
- Confirm the new behavior appears when you place an order.

**Goal:** Open/Closed—new reactions should not require editing the core placement flow.

---

## 3. Conditional reaction

Extend the domain or event so that **one** listener only runs when a condition holds (for example: quantity above a threshold, or a “premium” flag on the order).

**Goal:** Observers stay independent; the publisher does not become a giant `if` block.

---

## 4. Second event type

Introduce **`OrderCancelledEvent`** (or equivalent) published from a new endpoint (for example `DELETE` or `POST /orders/{id}/cancel`).

**Requirements:**

- At least **one** listener reacts only to cancellation.
- At least **one** listener might make sense for both placed and cancelled, or you document why it should stay order-placed only.

**Goal:** Same Observer mechanism, multiple notifications—design events intentionally.

---

## 5. Ordering

Two listeners must run in a **fixed** relative order (for example: validate inventory before warehouse picks).

**Goal:** Use Spring’s documented mechanism for listener order; justify why order matters for your scenario.

---

## 6. Async listeners

Make **one** listener asynchronous so slow work does not block the HTTP thread.

**Goal:** Know how async interacts with transactions and error handling (what happens if the async listener fails?).

---

## 7. Failure isolation

Design how you would prevent **one** listener’s failure from breaking others (or from failing the HTTP response).

**Goal:** Articulate trade-offs: synchronous vs async, transactions, error handlers—without necessarily implementing a production-grade solution.

---

## 8. From scratch (plain Java, optional)

Reimplement a minimal subject–observer pair **without** Spring: one list of observers, `subscribe` / `notify`, and three dummy observers. Compare mentally to `ApplicationEventPublisher` + `@EventListener`.

**Goal:** See that Spring is automating registration and dispatch—not inventing a different pattern.
