# Architecture Workshops

Welcome to the **Architecture** track. This is a sibling track to `design-patterns/` — while design patterns
answer *"how do I structure this class or object relationship?"*, architecture workshops answer
*"how do I structure this system?"*

---

## What this track covers

Architecture workshops zoom out to the level of **services, processes, and networks**. Each topic:

- Has a concrete **problem** rooted in real production systems
- Shows a **before** state (the naive / painful approach)
- Shows an **after** state (the architectural pattern that solves it)
- Includes **Mermaid diagrams** for visual flow
- Provides a runnable **implementation** (Spring Boot) of the critical seams
- Ends with **exercises** that cover real production concerns

---

## Categories planned

| Folder       | Topics                                                             |
|--------------|--------------------------------------------------------------------|
| `webhook/`   | Push-based event delivery across system boundaries (GitHub + CI)  |
| `event-bus/` | Durable async messaging (Kafka, SQS)                               |
| `api-gateway/` | Edge routing, auth, rate limiting                               |
| `saga/`      | Distributed transactions across services                           |
| `cqrs/`      | Command/Query Responsibility Segregation                           |
| `circuit-breaker/` | Fault tolerance between services                           |

---

## How to use these workshops

1. Read `problem.md` → understand the business scenario.
2. Read `context/README.md` → understand actors, boundaries, constraints.
3. Read `before/README.md` → understand why the naive approach fails.
4. Study `diagram/README.md` → understand the target flow visually.
5. Read `after/README.md` → understand the architectural decisions.
6. Explore `implementation/` → see the critical seams in runnable Spring Boot code.
7. Work through `exercise/README.md` → extend the system yourself.

---


## Contributing

See **[`architectures/CONTRIBUTING.md`](../CONTRIBUTING.md)** for how to add topics, the folder contract, branches, and PR expectations.

For repo-wide habits, you can also read [`design-patterns/CONTRIBUTING.md`](../../design-patterns/CONTRIBUTING.md).

Quick reminders for architecture topics:

- Use **Mermaid** for all diagrams (renders in GitHub, IntelliJ, VS Code).
- Keep implementation focused on the **seam** — the interface between components — not a full domain model.
- Every `before/` must have a named, specific failure mode (not just "it's bad").
- Every `after/` must address each failure mode called out in `before/`.
