# Problem: Payment Gateway Partner Notifications

###### (👀 don't cheat if you want to learn), take your time to learn.

You are on the platform team at a **payment gateway company**. Your `PaymentService` handles payment processing.
Three external partners have integrated with you:

- **ShopFront**: an e-commerce SaaS; needs to mark orders as paid the instant a payment succeeds.
- **FinanceTracker**: a revenue analytics platform; needs every payment event for dashboards.
- **FraudShield**:x  a fraud detection service; scores each payment as succeeded or failed.

### Current state (the `before/` problem)

The `PartnerIntegrationService` polls `GET /payments/{id}/status` for every open order every 5 seconds.
With 10,000 open orders across partners this produces millions of requests per hour → 99.9 % of which return "pending."
Latency between payment clearing and partner notification can be up to 5 seconds.
Adding a fourth partner means another polling loop inside the same class.

### Your design constraints

- **Core payment logic must not know about partners.** `PaymentService.process()` should announce "payment succeeded" and
  be done → it should not call ShopFront, FinanceTracker, or FraudShield directly.
- **Partners self-register** by providing a URL. Adding a new partner must require zero changes to producer code.
- **Delivery must be secure.** Each POST must be signed so partners can verify it came from you, not an attacker.
- **Transient failures must not lose events.** If a partner's server is down for 30 seconds, the notification must
  still arrive once the partner recovers.

### What you deliver in this workshop

Design a webhook dispatch system that: maintains a subscriber registry, signs outbound payloads with HMAC-SHA256,
delivers events via HTTP POST, and retries on failure → all without touching `PaymentService`. Relate that design to the
**webhook** (and, in the Spring track, to a full producer + consumer implementation).

---

## Part B: Dev platform → GitHub `push`, many CI systems, commit status

You are on the **developer platform** team. Teams use **GitHub** for source control. When someone pushes to `main`,
**multiple CI products** must start pipelines for the same commit: one team uses a hosted runner that behaves like
**CircleCI**, another uses an internal **generic** HTTP listener. Your product must not hard-code either vendor.

### Current pain

- Operations wants to **add a new CI** without redeploying the ingress service.
- GitHub delivers `push` webhooks quickly; if your fan-out is slow, GitHub **retries** and CI may see duplicates.
- CI must report **commit status** (`pending` → `success`/`failure`) so the PR page shows checks → that is usually a
  **REST call from CI to GitHub**, separate from the inbound `push` webhook.

### Constraints

- **Ingress verifies GitHub** using `X-Hub-Signature-256` on the raw body.
- **Each CI vendor** registers an `inboundWebhookUrl` (and optional signing secret for the hop from you to them).
- **Status updates** go to GitHub’s Statuses API shape → in the workshop, a fake in-process endpoint stands in for
  `api.github.com`.

### Deliverable for Part B

Study `context/README.md`, `diagram/README.md`, and run `implementation/` → trace one signed `push` through fan-out to
two simulated CI endpoints and into the fake status API. Then complete `exercise/README.md`.
