# Problem: User Profile — Notification Platform

###### (👀 don't cheat if you want to learn), take your time to think.

You are on the **platform team** at a B2C product company. The core service is `UserService`, which handles user
account management: registration, profile updates, email changes, and account deletion.

Several internal teams have integrated with `UserService` because they need to react when a user changes their
profile:

- **EmailTeam**: sends a "your profile was updated" confirmation email.
- **AnalyticsTeam**: tracks profile-change events for the user behaviour dashboard.
- **NotificationTeam**: sends an in-app notification ("your display name was updated").

---

## Current state (the `before/` problem)

The current `UserService.updateProfile()` calls each team's internal service directly via HTTP.
All calls are synchronous and inside the same `@Transactional` boundary.

With **500 active users updating their profiles per hour**, you are seeing:

- **Incident reports every week**: EmailService has occasional outages (5–10 minutes). During those outages,
  *no user can update their profile at all* — the DB transaction rolls back. Product team is escalating.
- **P95 latency of 7 seconds** on `PATCH /api/users/{id}`. Users report the UI feels "frozen".
  Email, analytics, and notification calls contribute ~6 s of that 7 s.
- **Duplicate notifications**: mobile clients retry on timeout; users report receiving 2–3 identical emails after
  a single profile update.
- **PII in analytics logs**: the analytics pipeline inadvertently logs the full user payload, including phone numbers.
  Security team has flagged this as a compliance risk.
- **New integration blocked**: the Legal team wants a "profile-changed" audit log for GDPR compliance. The
  engineering manager says "it's too risky to touch `UserService` again — it already has too many dependencies."

---

## Your design constraints

- **`UserService` must not know about consumers.** `updateProfile()` must have zero imports from email, analytics,
  or notification packages. Adding a fourth consumer must require zero changes to this class.
- **A consumer outage must not fail a user update.** If EmailService is down for 30 minutes, users can still update
  their profiles. Email is sent when EmailService recovers.
- **No duplicate side effects.** A client retry must result in at most one email, one analytics entry, one
  notification — regardless of how many times the HTTP call is retried.
- **No PII on the event bus.** Events in transit must not contain phone numbers, home addresses, or any field not
  required to identify the entity.
- **Consumers must be independently deployable.** Restarting `email-service` must not affect `analytics-service`.
- **The Legal audit log** (new consumer) must be addable as a new service without touching any existing code.

---

## What you deliver in this workshop

Design and implement an Event Notification system that:

1. Emits a minimal `UserProfileChangedEvent` from `UserService` to a message broker after each successful save.
2. Each consumer subscribes to its own independent queue, fetches user details from `UserService`'s REST API when
   needed, and guards against duplicate processing.
3. Consumer failures route to a Dead Letter Queue after retries — they never propagate back to the producer.
4. A `correlationId` threads through every hop — HTTP request → event emission → consumer processing — so a single
   log query identifies the full trace of any user update.
5. Adding the Legal audit consumer requires zero changes to `UserService`, `EmailService`, or `AnalyticsService`.

Study `before/` to understand the failure modes, then `after/` to see each design decision that resolves them.
Map every problem in `before/README.md` to the exact file and line in `after/` that fixes it.
