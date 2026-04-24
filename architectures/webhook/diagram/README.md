# Diagrams — Webhook architecture

**Note:** All diagrams use **Mermaid** so they render in GitHub and common editors.

---

## 1. GitHub `push` → multiple CI providers (fan-out)

Models **one ingress** that forwards the same verified payload to every registered pipeline URL — how you keep the
core small while onboarding many vendors.

```mermaid
sequenceDiagram
    autonumber
    participant Dev as Developer (git push)
    participant GH as GitHub
    participant Ing as Ingress (verify HMAC)
    participant Reg as Provider registry
    participant C1 as CI vendor A
    participant C2 as CI vendor B

    Dev->>GH: push commits
    GH->>Ing: POST /integrations/github/webhook<br/>X-Hub-Signature-256, body=push JSON
    Ing->>Ing: verify signature on raw bytes
    Ing->>Reg: resolve all inboundWebhookUrl
    par Fan-out
        Ing->>C1: POST inboundWebhookUrl (same JSON)
        Ing->>C2: POST inboundWebhookUrl (same JSON)
    end
    C1-->>Ing: 202 Accepted
    C2-->>Ing: 202 Accepted
    Ing-->>GH: 200 OK (ack fast)
```

---

## 2. CI → commit status (REST, not inbound webhook)

After the pipeline runs, the CI worker updates the commit using GitHub’s **Statuses** API. This is the usual “green
check” path.

```mermaid
sequenceDiagram
    autonumber
    participant C as CI worker
    participant GH as GitHub Statuses API

    C->>GH: POST /repos/{owner}/{repo}/statuses/{sha}<br/>{ state, context, description }
    GH-->>C: 201 Created
    Note over GH: UI shows check next to commit
```

In `implementation/`, `FakeGitHubStatusesController` plays the role of GitHub so you can run the loop locally.

---

## 3. Optional outbound signing to CI

If a CI vendor supports a shared secret on **their** inbound hook, your dispatcher can add `X-Outbound-Signature-256`
the same way GitHub signs to you.

```mermaid
flowchart LR
  D[Dispatcher] -->|HMAC body| H[X-Outbound-Signature-256]
  D -->|POST JSON| CI[CI inbound URL]
```

---

## 4. Payment gateway (parallel track, plain Java `after/`)

Same structural idea: registry + signed POST + independent consumers.

```mermaid
graph LR
  Pay[Payment core] --> Disp[WebhookDispatcher]
  Disp --> R[(WebhookRegistry)]
  R --> S1[ShopFront URL]
  R --> S2[FinanceTracker URL]
```
