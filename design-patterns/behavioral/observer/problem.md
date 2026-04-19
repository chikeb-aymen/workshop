# Problem: E‑commerce order fulfillment

###### (👀 don't cheat if you want to learn), take your time to learn.

You are on the team that owns **order placement** in an online store.

### Context

When a customer successfully places an order, the business expects several **follow‑up actions** to happen in the system, not necessarily in the database transaction alone, but as **logical consequences** of “order placed”:

- The customer should receive a **confirmation** (email is the first channel you ship).
- **Inventory** must reflect that items are allocated or leaving stock so you do not oversell.
- **Warehouse / logistics** need a signal to **prepare or schedule** fulfillment.
- **Analytics / reporting** must record the event for dashboards and funnel metrics.

Today these requirements are documented; tomorrow the product backlog will add more: SMS, fraud scoring, loyalty points, webhooks for partners. **Different teams** will own those reactions.

### Your design constraints

- **Core placement logic** should stay readable: validate, persist, acknowledge, not an endless sequence of bespoke calls to every subsystem that ever cares about orders.
- **New reactions** (new channel, new compliance step) should be addable **without** everyone editing the same giant class and fighting in the same merge window.
- Side-effect code should remain **replaceable and testable** (e.g. swap a real email gateway for a fake in tests) without rewriting the placement entry point each time.

### What you deliver in this workshop

Design and code a shape where **one** “order placed” fact can trigger **many** independent reactions, with **minimal coupling** between the component that commits the order and the components that react, and relate that shape to the **Observer** pattern (and, in the Spring track, to **events and listeners**).

Read **`README.md`** for terminology and trade-offs; use **`before/`**, **`after/`**, and **`spring-example/`** as references once you have attempted your own direction.
