# Contributing to the Design Patterns Workshop

Thanks for helping grow this repo. The goal is **clear problems**, **honest before/after code**, and **consistent folder layout**.

## Adding a new pattern

1. **Copy** [`template/`](template/) into the right category folder (`creational/`, `structural/`, or `behavioral/`) and rename it to the pattern name (`kebab-case`).
2. **Read** [`template/README.md`](template/README.md) and follow the **author checklist**.
3. Use [`behavioral/strategy/`](behavioral/strategy/) as a **complete example** of structure and tone.
4. Replace the scaffold in **`problem.md`** with your scenario; fill in the main **`README.md`**.

## Branches and PRs

- Use a descriptive branch name, e.g. `feature/observer-pattern` or `docs/strategy-fixes`.
- In the PR description, include:
  - Pattern name and **GoF category**
  - What learners should take away
  - Optional: screenshots if you changed diagrams

## Style

- Prefer **small, readable** samples over enterprise-sized examples.
- Keep **before** and **after** comparable so the pattern is easy to see.
- Avoid unrelated refactors in the same PR as a new pattern.

## Questions

Open an issue for **category uncertainty**, **large additions**, or **breaking layout changes** before investing a lot of time.
