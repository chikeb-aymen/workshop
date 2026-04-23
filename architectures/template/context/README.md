# Context

## Business scenario

[2-3 sentences. The company, the product, and the team. Why does this system exist?]

---

## Actors and boundaries

| Actor | Type | Responsibility |
|-------|------|----------------|
| [Name] | Internal service / External system / User | [What they do] |
| [Name] | Internal service / External system / User | [What they do] |

---

## System boundaries

```
[ASCII or Mermaid component diagram showing what is inside vs outside the trust boundary]
```

---

## Key constraints

- **Latency:** [requirement, e.g. "partner must know within 1 second"]
- **Throughput:** [e.g. "up to 10,000 events per minute"]
- **Reliability:** [e.g. "no event loss; retries allowed"]
- **Security:** [e.g. "each partner has a separate secret key"]
- **Extensibility:** [e.g. "new partners must self-onboard without code changes"]

---

## Data that flows

| Event / Message | Source | Destination | Payload shape |
|-----------------|--------|-------------|---------------|
| [Name] | [System] | [System] | [Brief shape] |
