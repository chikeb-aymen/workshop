# Diagrams

All diagrams use **Mermaid** syntax — renders natively in GitHub, IntelliJ, VS Code (with the Mermaid plugin),
and most modern documentation tools.

---

## Flow 1: [Name — e.g. "Happy path"]

```mermaid
sequenceDiagram
    autonumber
    participant Producer
    participant [Component]
    participant Consumer

    Producer->>+[Component]: event(payload)
    [Component]-->>-Producer: ack

    [Component]->>+Consumer: POST /webhook
    Consumer-->>-[Component]: 200 OK
```

---

## Flow 2: [Name — e.g. "Retry on failure"]

```mermaid
sequenceDiagram
    autonumber
    participant Dispatcher
    participant Consumer

    Dispatcher->>Consumer: POST (attempt 1)
    Consumer-->>Dispatcher: 503 Service Unavailable
    Note over Dispatcher: wait 2s (backoff)
    Dispatcher->>Consumer: POST (attempt 2)
    Consumer-->>Dispatcher: 200 OK
```

---

## Component diagram

```mermaid
graph TD
    A([Producer]) -->|dispatch event| B[Dispatcher]
    B --> C[(Registry)]
    C -->|resolve subscribers| B
    B -->|signed POST| D([Subscriber A])
    B -->|signed POST| E([Subscriber B])
```

---

## Adding a new diagram

Copy one of the blocks above. Mermaid supports: `sequenceDiagram`, `graph`, `flowchart`, `erDiagram`,
`classDiagram`, `stateDiagram-v2`, `gantt`. Use the simplest type that communicates the idea.
