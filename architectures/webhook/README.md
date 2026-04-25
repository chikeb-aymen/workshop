# Webhook - Event Architecture

###### Category: Architectures

## 1. Intent

Notify **external subscribers** about events happening inside your system by sending a signed **HTTP POST** to a
pre-registered URL → the moment the event occurs → without the producer knowing anything about the consumer's internal
logic, technology, or how many consumers exist.
In practice: **"don't call us, we'll call you"** → at the HTTP level.

---

## 2. Why it exists (the real pain)

Your payment gateway processes a payment. Three partners need to know: the e-commerce platform wants to mark the order
paid, the analytics service wants to log revenue, the fraud engine wants to score the transaction.
The naive approach is to hard-code calls to each partner's API inside `PaymentService`. But:

- **Partners are external** → you do not control their uptime, API shape, or auth.
- **New partners onboard continuously** → adding one must not mean editing your core payment code.
- **Partners must be notified even if their service is temporarily down** → you need retry semantics.
- **Partners must be able to verify the call came from you, not an attacker** → you need signatures.
  Webhooks solve all four problems: an open subscription model, HTTP push, built-in retry, and HMAC-based security.

---

## 3. What it is (in one breath)

Your system fires an HTTP POST to every URL registered for a given event type. The POST carries a signed JSON payload
describing what happened. Subscribers process it independently and asynchronously. New subscribers self-register via
an API → the producer's core logic never changes.

---

## 4. Structure (roles)

| Role                  | Responsibility                                                                                  |
|-----------------------|-------------------------------------------------------------------------------------------------|
| **WebhookRegistry**   | Stores the mapping `eventType → [subscriberUrl, secret]`. Supports register / unregister.       |
| **WebhookSigner**     | Computes `HMAC-SHA256(secret, rawBody)` → lets consumers verify authenticity.                   |
| **WebhookDispatcher** | Iterates subscribers for an event, signs each payload, POSTs it, and handles retries.           |
| **WebhookEvent**      | The payload DTO: `eventId`, `eventType`, `createdAt`, `data`. Self-describing, version-stamped. |
| **WebhookSubscriber** | A registered consumer: `id`, `url`, `eventType`, `secretKey`.                                   |
| **WebhookHandler**    | Consumer-side interface: `supports(eventType)` + `handle(event)`. One impl per event type.      |

**Relationship to Observer Pattern:**

- `WebhookRegistry` = Subject's observer list.
- `WebhookDispatcher.dispatch(event)` = Subject's `notifyObservers()`.
- `WebhookSubscriber` URL = the Observer, living across a network boundary.

---

## 5. Applicability (when to reach for it)

Use Webhooks when:

- You need to **notify external systems** (different company, different service) in near-real-time.
- **Consumers are unknown at design time**, any partner should be able to register without you writing code.
- **Polling is wasteful**, events are infrequent relative to the polling interval, or real-time latency matters.
- A **trust boundary** exists you need to prove the call came from you (HMAC signatures).
  Avoid or complement when:
- High-throughput fanout consider a **message queue** (Kafka, SQS) fronting the dispatcher for durable buffering.
- Purely internal in-process fanout, plain **Observer** or Spring **ApplicationEventPublisher** is simpler.
- Consumers need **replay** from an offset, a log-structured bus (Kafka) is a better fit than stateless HTTP POSTs.

---

## 6. Consequences (benefits and sharp edges)

### Benefits

- **Open subscription model:** partners self-register; the producer's core logic is closed to modification.
- **Real-time:** consumers learn the instant something happens — no polling latency.
- **Decoupled:** producer doesn't import any consumer code; it only knows a URL and an event shape.
- **Secure by default:** HMAC signatures let each consumer verify authenticity independently.
- **Resilient:** retry-with-backoff means transient consumer downtime does not lose events.

### Liabilities
###### (take in consideration)
- **At-least-once delivery:** retries mean consumers can receive the same event twice. Always design handlers to be
  idempotent (check `eventId` before processing).
- **No ordering guarantee:** two events dispatched close in time may arrive out of order at the consumer.
- **Consumer latency visible to producer:** if POST takes 30 s, your dispatcher thread blocks for 30 s unless you async.
  Production systems send to a queue first; a separate worker does the HTTP delivery.
- **Secret rotation is operational overhead:** rotating the shared HMAC key requires coordination between producer and
  consumer.

---

## 7. Mapping to this workshop

| Folder            | What to study                                                                                     |
|-------------------|---------------------------------------------------------------------------------------------------|
| `context/`        | Two scenarios (payments + GitHub/CI), actors, boundaries, data-flow table.                        |
| `before/`         | Polling-based integration: `PartnerIntegrationService` polls the payment API every 5 s. Wasteful. |
| `after/`          | Webhook dispatcher + registry + HMAC signer + consumer-side handlers. Pure Java, no framework.    |
| `diagram/`        | GitHub `push` fan-out, CI → commit status (REST), optional outbound signing (Mermaid).            |
| `implementation/` | **You implement it** — see `implementation/README.md` (sources removed so you type the seams yourself). |
| `exercise/`       | Idempotency, async fan-out, third vendor, outbound signature verify, real GitHub tunnel.          |

---

## 8. Where you meet Webhooks in the wild

- **Stripe / PayPal:** `payment.succeeded`, `payment.failed`, dispute events — signed POST to your URL.
- **GitHub / GitLab:** `push`, `pull_request`, `workflow_run`, `check_run`, … — **CI triggers** are webhooks: the host
  POSTs JSON to CircleCI / Jenkins / your ingress. **Commit status** is usually the CI system calling **GitHub’s REST
  API** afterward; GitHub may emit *another* webhook (`status`, `check_run`) to your integrations when that updates.
- **Shopify:** order/created, order/fulfilled, product/updated.
- **Twilio:** message delivery status, call status.
- **Any SaaS with a “Webhooks” settings page.**

For GitHub + CircleCI specifically: CircleCI receives GitHub’s repository webhooks (or you configure a relay). The
important architectural split is unchanged: **push notification** (webhook) vs **status reporting** (REST callback).

---

## 9. Problem statement
See **`problem.md`** for the workshop scenario (read it before diving into `before/` and `after/`).
