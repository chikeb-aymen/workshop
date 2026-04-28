# Exercises - State Pattern

Work through these in order. Implement and run code yourself; avoid copying the full `after/` solution until you have
tried the refactor.

**Prerequisite:** Read **`problem.md`** and skim `before/` so you know the intended phases and operations.

---

## 1. Trace the delegation path

Starting from `after/com/workshop/tcp/App.java`, list **each call** from `TCPConnection` into a concrete state until the
program finishes. For each transition, name **which** concrete state class decided the next state.

**Goal:** You can explain “context delegates → state handles → state may change context’s state” without opening this
README.

---

## 2. Compare to `before/` in one paragraph

Open `before/TCPConnection.java` and describe **where** phase is checked and **how many** methods repeat that structure.
Contrast with `after/com/workshop/tcp/context/TCPConnection.java` in a short paragraph.

**Goal:** Articulate what the State pattern **removed** from the context class.

---

## 3. Add a new phase (design only, then code)

Design a fourth phase (for example **CLOSING** or **SYN_SENT**) that fits the toy protocol. List:

- which **existing** operations change when this phase exists, and  
- which **transitions** enter and leave it.

Then add it to **`before/`** only: extend the `enum` and every `switch` that must change.

**Goal:** Feel the maintenance cost of the naive approach before refactoring that change into `after/`.

---

## 4. Implement the same phase in the State pattern

Repeat exercise 3 in **`after/`**: new `ConcreteState`, transitions from at least two other states, and any new hooks on
`TCPState` if needed. Update `App` to demonstrate entering and leaving the new phase.

**Goal:** See that new behavior is mostly **new or localized classes**, not a sweep across every method on the context.

---

## 5. Illegal operation

Pick one operation that should be a **no-op** (or should log “ignored”) in a phase where the workshop sample is silent.
Implement explicit behavior (for example log line) in **one** concrete state and rely on defaults elsewhere.

**Goal:** Practice overriding only what a state needs; avoid empty noise in unrelated states.

---

## 6. Who owns transitions?

In the sample, concrete states call `changeState` on the context. Sketch an alternative where **only** `TCPConnection`
decides the next state after inspecting the current state (states return “events” or enums instead).

**Goal:** Explain one **benefit** and one **drawback** of centralizing transitions in the context (Gamma et al. discuss
this trade-off).

---

## 7. State vs Strategy (written)

Imagine two features: (a) payment method selection at checkout, (b) TCP-style connection lifecycle. For each, argue
**State** or **Strategy** (or neither) and tie your answer to the comparison table in the main **`README.md`**.

**Goal:** You do not confuse the two patterns just because both use delegation.

---

## 8. Optional: table-driven alternative

Describe (pseudocode or a small table) a **transition matrix**: rows = phase, columns = operation, cells = next phase
or “error.” Compare maintainability to the polymorphic `after/` layout for **three** phases vs **ten** phases.

**Goal:** Understand when a table is attractive and when polymorphic states stay clearer.
