# After: Webhook Dispatcher

## What changed

- `WebhookRegistry` owns the subscriber list — `PaymentService` (or any producer) never touches partner URLs.
- `WebhookDispatcher.dispatch(event)` iterates subscribers, signs each payload with `WebhookSigner`, and POSTs.
- Adding a new partner = `registry.register(new WebhookSubscriber(...))` — zero changes to any existing class.
- `WebhookHandler` implementations live on the **consumer side** and react to the typed event.
- Payload carries a stable `eventId` for consumer-side idempotency guards.

## The key insight

`PaymentService.process()` calls `dispatcher.dispatch("payment.succeeded", data)` — one line.
Everything else — who receives it, how many there are, what they do — is the registry's concern.

---

## Parallel track: GitHub → CI → commit status

Part B of `problem.md` is the **dev-platform** angle: GitHub (or any git host) POSTs a `push` webhook; your ingress
verifies `X-Hub-Signature-256`, then a **registry of CI inbound URLs** receives the same JSON. Each CI system later
calls GitHub’s **Statuses** REST API (simulated in `../implementation/`). The same registry + dispatcher idea applies —
only the event shape and signature header names change.
