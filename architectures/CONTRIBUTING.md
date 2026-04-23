# Contributing to the Architecture Workshops

Thanks for helping grow this track. The goal is **clear system-level problems**, **honest before/after narratives**, **Mermaid diagrams learners can render anywhere**, and **the same folder layout** across every topic.

---

## Adding a new architecture topic

1. **Copy** [`template/`](template/) into `architectures/` and rename the folder to the topic name (**kebab-case**), e.g. `event-bus/`, `api-gateway/`, `saga/`.
2. **Read** [`template/README.md`](template/README.md) and fill every section placeholder with real content.
3. Use [`webhook/`](webhook/) as the **reference implementation** for structure, tone, and depth:
   - `problem.md` → concrete scenario and constraints
   - `context/README.md` → actors, boundaries, data flows
   - `before/README.md` → named failure modes (prefer a table)
   - `after/README.md` → decisions, options considered, trade-offs accepted
   - `diagram/README.md` → Mermaid only (`sequenceDiagram`, `flowchart`, `graph`)
   - `implementation/` → runnable Spring Boot app focused on **seams** (interfaces, wiring), not a full product
   - `exercise/README.md` → production-shaped follow-ups (idempotency, retries, secrets, scaling)

4. Replace scaffold text in **`problem.md`** and the main **`README.md`**; delete any template-only sentences.

---

## Folder contract (must match)

Every topic under `architectures/<topic>/` should contain:

| Path | Purpose |
|------|---------|
| `README.md` | Intent, pain, applicability, consequences, mapping to folders |
| `problem.md` | Scenario the learner solves |
| `context/README.md` | Actors, trust boundaries, constraints, data that flows |
| `before/README.md` | Naive approach + failure modes + optional metrics |
| `after/README.md` | Target architecture + key decisions + trade-offs |
| `diagram/README.md` | Mermaid diagrams (no proprietary formats required) |
| `implementation/` | Optional but encouraged: `pom.xml`, `README.md`, focused Spring Boot code |
| `exercise/README.md` | Hands-on tasks that extend the architecture |

If a topic truly has no runnable code, keep `implementation/README.md` explaining why and what learners should sketch instead.

---

## Branches and PRs

- Use a descriptive branch name, e.g. `feature/architectures-event-bus` or `docs/webhook-diagram-fix`.
- In the PR description, include:
  - Architecture topic name and **category** (integration, reliability, scalability, security, or your best fit)
  - What learners should take away in one or two sentences
  - Whether `implementation/` was added or intentionally omitted
  - Optional: link or paste a rendered Mermaid diagram if review is easier with a screenshot

---

## Style

- Prefer **small, runnable** implementations over a full production system. One or two **seams** (interfaces, boundaries) matter more than complete domain models.
- Keep **`before/`** and **`after/`** comparable: the same scenario, so the architectural shift is obvious.
- **Diagrams:** use Mermaid in Markdown so GitHub and common editors render them without extra assets.
- Avoid unrelated refactors in the same PR as a new architecture topic.
- Do not change **`template/`** unless you are improving the reusable scaffold for **all** future topics; call that out explicitly in the PR.

---

## Questions

Open an issue for **unclear category placement**, **large multi-topic additions**, or **breaking changes to the folder contract** before investing a lot of time.
