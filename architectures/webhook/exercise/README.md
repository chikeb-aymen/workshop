# Exercises — Webhook architecture

---

## Exercise 1 — Idempotent CI triggers

**Addresses:** duplicate deliveries when GitHub or your dispatcher retries.

**Task:** Add a `delivery_id` (or GitHub’s `X-GitHub-Delivery` header) to the fan-out path. In each simulated CI
endpoint, reject processing if that id was seen in the last 24 hours.

**Hint:** Start with an in-memory `ConcurrentHashMap<String, Instant>` before reaching for Redis.

---

## Exercise 2 — Queue between ingress and fan-out

**Addresses:** slow CI endpoints blocking GitHub’s delivery timeout.

**Task:** After signature verification, publish the raw payload to an in-memory queue and return `200 OK` immediately.
A `@Scheduled` worker performs the HTTP fan-out.

**Hint:** GitHub expects a response within seconds; long work belongs off the request thread.

---

## Exercise 3 — Register a third “vendor” without editing ingress code

**Addresses:** Open/Closed at the architecture boundary.

**Task:** Use `PUT /admin/pipeline-providers` to add `buildkite-demo` pointing at a new `POST` mapping you add under
`/sim-ci/buildkite/pipeline`. Trigger a push and confirm three status contexts appear in `GET /demo/commit-status-log`.

---

## Exercise 4 — Verify `X-Outbound-Signature-256` on the simulated CI side

**Addresses:** mutual authentication between your platform and a partner CI endpoint.

**Task:** When `outboundSigningSecret` is set on a provider, compute HMAC in the simulated CI controller and reject
requests with a bad signature using the same constant-time comparison as ingress.

---

## Exercise 5 — Map to real GitHub

**Addresses:** closing the gap between the workshop and production.

**Task:** Create a test repo, add a webhook pointing to a tunnel (ngrok, Cloudflare Tunnel) to your local ingress, set
the same `app.github.webhook-secret` as in GitHub’s UI, and confirm a real `push` is accepted. **Do not** expose admin
routes publicly.

---

## Stretch — Subscribe to `check_run` webhooks

**Task:** After status updates, GitHub can notify *your* product via `check_run` events. Add a second controller path
that validates those payloads and logs transitions — useful for internal analytics dashboards.
