# Implementation

This folder contains a runnable **Spring Boot** application demonstrating the critical seams of the
architecture. It is not a full production application — it focuses on the interfaces and wiring that
make the pattern work.

---

## What is here

| Package / Class | Role |
|-----------------|------|
| `[class]` | [what it demonstrates] |
| `[class]` | [what it demonstrates] |

---

## Running the application

```bash
cd implementation
mvn spring-boot:run
```

---

## Key endpoints

| Method | Path | What it demonstrates |
|--------|------|----------------------|
| POST | `/[path]` | [description] |
| GET  | `/[path]` | [description] |

---

## What to focus on when reading the code

1. **[Class/interface name]** — this is the architectural seam. Everything plugs in here.
2. **[Class name]** — this is where [key decision] is enforced.
3. **[Class name]** — this shows how to extend the system without modifying existing code.

---

## What is intentionally left out

- No persistence (in-memory only) — the focus is the communication pattern, not the data layer.
- No authentication beyond the pattern's own security mechanism.
- No production-grade error handling — see `exercise/` to add it.
