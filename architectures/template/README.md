# [Architecture Name]

###### Category: [e.g. Integration / Reliability / Scalability / Security]

## 1. Intent

One paragraph. What architectural problem does this pattern solve, and what is the core mechanism?
Write it so a senior engineer who has never heard the name can immediately place it.

---

## 2. The Real Pain

Describe the concrete production failure mode this pattern exists to fix.
Be specific: name the service, the operation, the symptom. Avoid generic statements like "it was hard to maintain."

---

## 3. Core Idea (in one breath)

One sentence. No jargon. If you cannot write it in one sentence, the pattern is not clearly understood yet.

---

## 4. Key Components

| Component | Role |
|-----------|------|
| **[Name]** | What it owns and what it does |
| **[Name]** | What it owns and what it does |

---

## 5. Applicability — when to reach for it

**Use when:**
- (condition 1)
- (condition 2)

**Avoid when / complement with:**
- (condition 1)
- (condition 2)

---

## 6. Consequences

**Benefits**
- ...

**Liabilities / sharp edges**
- ...

---

## 7. Mapping to this workshop

| Folder             | What to study                              |
|--------------------|--------------------------------------------|
| `context/`         | Business scenario, actors, constraints     |
| `before/`          | The problematic approach and its failures  |
| `after/`           | The architectural decisions and trade-offs |
| `diagram/`         | Visual flows (Mermaid)                     |
| `implementation/`  | Runnable Spring Boot implementation        |
| `exercise/`        | Hands-on practice                          |

---

## 8. Where you meet this in the wild

- (real-world example 1)
- (real-world example 2)

---

## 9. Problem statement

See **`problem.md`** for the scenario. Read it before the diagrams and code.
