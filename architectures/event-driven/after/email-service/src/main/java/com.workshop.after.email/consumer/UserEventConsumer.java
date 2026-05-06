package com.workshop.after.email.consumer;

import com.workshop.after.email.client.UserServiceClient;
import com.workshop.after.email.idempotency.ProcessedEvent;
import com.workshop.after.email.idempotency.ProcessedEventRepository;
import com.workshop.after.user.event.UserProfileChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Idempotency:
 *      existsByEventId check + insert in the same @Transactional block.
 *      Duplicate event = early return, no double-email.
 * <p>
 * Ordering / staleness (latest-state):
 *      We fetch current state from UserService, not from the event.
 *      If user was updated twice quickly, both events lead to the same fetch;
 *      both are processed idempotently and the latest state is used.
 * <p>
 * Schema evolution:
 *      Unknown schemaVersion is rejected and routed to DLQ for manual inspection.
 *      This prevents silent misprocessing when the event shape changes.
 * <p>
 * Resilience:
 *
 * @Retryable retries on transient errors (network, DB blip) with backoff.
 *      After maxAttempts, Spring AMQP routes the message to DLQ
 *      (because setDefaultRequeueRejected=false in RabbitMQConfig).
 * <p>
 * Observability:
 *      correlationId is put into MDC so every LOGGER line in this thread carries it.
 */
@Component
public class UserEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEventConsumer.class);
    private static final int SUPPORTED_SCHEMA_VERSION = 1;
    private static final String CONSUMER_NAME = "email-service";

    private final ProcessedEventRepository processedEventRepository;
    private final UserServiceClient userServiceClient;

    public UserEventConsumer(ProcessedEventRepository processedEventRepository,
                             UserServiceClient userServiceClient) {
        this.processedEventRepository = processedEventRepository;
        this.userServiceClient = userServiceClient;
    }

    @RabbitListener(queues = "email-service.user.events")
    @Transactional
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    public void handle(UserProfileChangedEvent event) {
        MDC.put("correlationId", event.getCorrelationId());
        MDC.put("eventId", event.getEventId());

        try {
            // Schema evolution: reject unknown versions explicitly
            if (event.getSchemaVersion() != SUPPORTED_SCHEMA_VERSION) {
                LOGGER.error("[SKIP] Unknown schemaVersion={} eventId={} — routing to DLQ", event.getSchemaVersion(), event.getEventId());
                throw new UnsupportedOperationException("Unknown schema version: " + event.getSchemaVersion());
            }

            // Idempotency: check before processing
            if (processedEventRepository.existsByEventId(event.getEventId())) {
                LOGGER.info("[SKIP] Duplicate eventId={} — already processed", event.getEventId());
                return;
            }

            LOGGER.info("[PROCESS] eventId={} userId={} changeType={}", event.getEventId(), event.getEntityId(), event.getChangeType());

            // Fetch-back / clear ownership:
            // We don't trust the event for data. We fetch from source of truth.
            UserServiceClient.UserDto user = userServiceClient
                    .fetchUser(event.getEntityId(), event.getCorrelationId())
                    .orElse(null);

            if (user == null) {
                LOGGER.warn("[SKIP] User not found (deleted?) userId={}", event.getEntityId());
                // Mark as processed so we don't retry a deleted user indefinitely
                markProcessed(event.getEventId());
                return;
            }

            // Business logic: send email
            sendProfileChangedEmail(user, event);

            // Idempotency: mark processed AFTER success, in same transaction
            markProcessed(event.getEventId());

        } finally {
            MDC.clear();
        }
    }

    private void sendProfileChangedEmail(UserServiceClient.UserDto user,
                                         UserProfileChangedEvent event) {
        // Real implementation: call SMTP / SendGrid / SES here
        LOGGER.info("[EMAIL] Sending profile-changed email to={} correlationId={}", user.email(), event.getCorrelationId());
    }

    private void markProcessed(String eventId) {
        processedEventRepository.save(new ProcessedEvent(eventId, CONSUMER_NAME, Instant.now()));
    }
}
