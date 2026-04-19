# Pattern folder template

Copy this entire folder when adding a new design pattern to the workshop. Rename the copy to match the pattern (use **kebab-case**, e.g. `observer`, `factory-method`).

**Reference implementation:** [`../behavioral/strategy/`](../behavioral/strategy/) — use it when something below is unclear.

---

## Where to put the copy

| GoF category   | Target path |
|----------------|-------------|
| Creational     | `design-patterns/creational/<pattern-name>/` |
| Structural     | `design-patterns/structural/<pattern-name>/` |
| Behavioral     | `design-patterns/behavioral/<pattern-name>/` |

Create the category folder (`creational/`, `structural/`, …) the first time you add a pattern there if it does not exist yet.

If you are unsure about the category, open an issue or ask in your PR description.

---

## Folder layout

| Path | Purpose |
|------|---------|
| `README.md` | Main lesson: intent, problem teaser, solution, structure, UML link, benefits, drawbacks, real-world hooks. |
| `problem.md` | Scenario for learners (requirements, constraints). Replace the scaffold in `problem.md`. |
| `before/` | “Naive” or overgrown code that motivates the pattern. Small, runnable snippets are ideal. |
| `after/` | Same problem refactored with the pattern applied. Keep structure easy to navigate. |
| `exercise/` | Tasks, questions, or partial code for readers (optional but encouraged). |
| `diagram/` | Optional: UML or sketch (`*.png`, `*.svg`, etc.) referenced from `README.md`. |
| `spring-example/` | Optional: Spring-style wiring if it helps (e.g. dependency injection). Not required for every pattern. |

---

## Author checklist

- [ ] `README.md` states **category** (Creational / Structural / Behavioral) and **intent** in plain language.
- [ ] `problem.md` gives a **concrete scenario** and **success criteria** (what “good” looks like).
- [ ] `before/` shows the pain point without the pattern (short is fine).
- [ ] `after/` shows the pattern **without** unrelated refactors.
- [ ] If you add images, they live under `diagram/` and use **relative paths** from `README.md`.
- [ ] Optional sections are either present and maintained or not mentioned in `README.md`.

---

## Naming

- **Folder name:** `kebab-case`, matches the pattern name (e.g. `abstract-factory`).
- **Files:** match the workshop style already in the repo (see Strategy).

---

## Suggested `README.md` outline

Use Strategy’s `README.md` as a skeleton: **intent → problem → solution → structure → UML → benefits → drawbacks → real-world examples (and optional “when not to use”).**

---

## Contributing

See [`../CONTRIBUTING.md`](../CONTRIBUTING.md) for branch naming, PR expectations, and review hints.
