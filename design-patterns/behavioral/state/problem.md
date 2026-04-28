# Problem: TCP connection lifecycle

###### (👀 don't cheat if you want to learn), take your time to learn.

**You are modeling a simplified TCP connection**.(This is from the design pattern book: Element of Reusable Object-Oriented Software)

A connection can be in one of several phases:

- **Closed**: no active session
- **Listening**: passively waiting for a peer
- **Established**: data may flow

**Operations clients may invoke** (names aligned with the classic GoF example):

- `activeOpen`, `passiveOpen`, `close`, `send`, `acknowledge`, `synchronize`, `transmit`

**How each operation behaves depends on the current phase.** For example, `activeOpen` is meaningful from **Closed** but
not from **Established** in this simplified model.

**Pain you should feel in the naive code:**

- The same `switch` (or `if` chain) on the current phase appears in **many** methods
- Adding a new phase or changing a transition touches **several** places
- The class grows a monolithic conditional structure that is hard to read and extend

Your task later will be to refactor toward the **State** pattern: delegate phase-specific behavior to dedicated state
objects.
