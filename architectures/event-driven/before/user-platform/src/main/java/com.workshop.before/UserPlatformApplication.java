package com.workshop.before;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BEFORE: A synchronous, tightly-coupled user platform.
 *
 * This application demonstrates every problem that Event Notification solves.
 * When a user profile is updated, UserService directly and synchronously
 * calls three downstream services via HTTP — all inside the same
 * @Transactional boundary.
 *
 * Problems to observe:
 *
 *   PROBLEM 1 — TIGHT COUPLING
 *     UserService imports and constructs EmailClient, AnalyticsClient, and
 *     NotificationClient directly. Adding an SmsService means opening
 *     UserService and modifying it. This violates the Open/Closed principle
 *     and makes the system fragile as it grows.
 *
 *   PROBLEM 2 — CASCADING FAILURES
 *     All three HTTP calls are inside @Transactional. If EmailService responds
 *     with a 500, Spring rolls back the database transaction. The user's profile
 *     update is lost — not because the update was invalid, but because an
 *     unrelated downstream service failed.
 *
 *   PROBLEM 3 — LATENCY MULTIPLICATION
 *     The user waits for: DB write + email call + analytics call + notification
 *     call. If each downstream takes 2 seconds, the user waits at least 6+
 *     seconds for a simple profile update. All calls are sequential.
 *
 *   PROBLEM 4 — NO IDEMPOTENCY
 *     If the client retries the PATCH /api/users/{id} request (common with
 *     network timeouts), every retry triggers another email, another analytics
 *     event, another notification. No deduplication exists anywhere.
 *
 *   PROBLEM 5 — PII LEAKAGE
 *     The full User object — including phone number and home address — is
 *     serialized and sent over the network to EmailService and AnalyticsService.
 *     Those services may only need an email address. Sensitive data spreads to
 *     every service.
 *
 *   PROBLEM 6 — THUNDERING HERD
 *     Every user update triggers synchronous calls to all three services at
 *     exactly the same moment. A traffic spike in the profile update endpoint
 *     immediately fans out to every downstream service simultaneously.
 *
 *   PROBLEM 7 — NO ORDERING CONTROL
 *     Two concurrent PATCH requests for the same user can interleave. There is
 *     no version check, no optimistic locking, no way for downstream services
 *     to detect that they processed a stale update.
 *
 * Open UserService.java and read the comments — every problem is annotated.
 * Then compare with the after/ solution.
 */
@SpringBootApplication
public class UserPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserPlatformApplication.class, args);
    }
}
