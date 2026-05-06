# Before: Synchronous Coupled User Platform

## What you are looking at

`UserPlatformApplication` is a single Spring Boot application. When a user updates their profile,
`UserService.updateProfile()` saves the change to the database and then **synchronously calls three downstream
services** — `EmailClient`, `AnalyticsClient`, `NotificationClient` — in sequence, all inside the same
`@Transactional` method.

Start here: open `service/UserService.java`. Every problem is annotated in the code.

---

## Smells to notice

### 1. Tight coupling — UserService knows about every consumer
`UserService` imports `EmailClient`, `AnalyticsClient`, and `NotificationClient`.
If the product team adds an `SmsService`, a developer must open `UserService`, add a new field, inject a new bean,
add a new call, and redeploy. This never ends — the class grows with every new integration.
**The producer has become the integration hub.** This is the root cause of all the other problems below.

### 2. Cascading failure — one downstream outage kills the whole update
All three HTTP calls sit inside `@Transactional`. If `EmailService` throws a `RuntimeException`,
Spring rolls back the database transaction. **The user's profile is not saved** — not because their input was
invalid, but because an unrelated email notification service happened to fail at that moment.
This means a 500 from a notification side-effect breaks a core write operation.

### 3. Latency multiplication — the user waits for everything
The update flow is fully sequential:
```
DB write → email call (2 s) → analytics call (3 s) → notification call (1 s) → response
```
The user waits at least **6 seconds** for a profile update. None of those downstream calls are part of the user's
actual request. If any service is slow (GC pause, cold start, network jitter), every user waits longer.

### 4. No idempotency — duplicates are built in
There is no `Idempotency-Key` on the request and no deduplication guard anywhere. If the HTTP client times out and
retries, `UserService` will:
- write to the DB again (or update again),
- call `EmailClient` again → second email sent,
- call `AnalyticsClient` again → duplicate analytics entry,
- call `NotificationClient` again → duplicate in-app notification.

Network retries are normal and expected. The system has no defense against them.

### 5. PII leakage — full user object pushed to every service
The full `User` entity — including `phone` and `address` — is serialized and sent over the network to
`EmailService`, `AnalyticsService`, and `NotificationService`. These services may only need a display name or email
address. Sensitive data now lives in every service's logs, databases, and HTTP traffic.

### 6. Thundering herd — load amplified immediately
A traffic spike on `PATCH /api/users/{id}` fans out to three downstream services at exactly the same instant.
If 1,000 users update their profiles simultaneously, 3,000 synchronous HTTP calls go out at the same moment.
There is no buffering, no rate limiting, no backpressure anywhere in the chain.

### 7. No ordering control — concurrent updates race
Two concurrent `PATCH` requests for the same user can interleave at the DB level (mitigated by JPA `@Version` here,
but not at the downstream call level). `EmailService` might receive the calls in a different order than the DB saw
them. Downstream services have no way to detect which notification is for the newer vs older state.

---

## What to compare

After studying these problems, open `after/` and trace:

1. How `UserService` was replaced by three independent services.
2. How the same downstream work happens — but now asynchronously, with no coupling.
3. How every problem above is resolved by a specific design decision in the after code.

The goal is not just to see "it works differently" — it is to be able to name **which file and which line** fixes
each specific problem you found here.
