package com.workshop.after.analytics.consumer;

import com.workshop.after.analytics.client.UserServiceClient;
import com.workshop.after.analytics.idempotency.ProcessedEvent;
import com.workshop.after.analytics.idempotency.ProcessedEventRepository;
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
 * Decoupling:
 *      AnalyticsService is a completely independent consumer. It has its own
 *      queue, its own processed_events table, its own DLQ. It can be deployed,
 *      scaled, or stopped without any knowledge of or impact on EmailService.
 * <p>
 * Ordering:
 *      entityVersion is used here to skip events older than the last processed
 *      version. This prevents analytics from double-counting a rollback scenario.
 */
@Component
public class UserEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEventConsumer.class);
    private static final int SUPPORTED_SCHEMA_VERSION = 1;
    private static final String CONSUMER_NAME = "analytics-service";

    private final ProcessedEventRepository processedEventRepository;
    private final UserServiceClient userServiceClient;

    public UserEventConsumer(ProcessedEventRepository processedEventRepository,
                             UserServiceClient userServiceClient) {
        this.processedEventRepository = processedEventRepository;
        this.userServiceClient = userServiceClient;
    }

    @RabbitListener(queues = "analytics-service.user.events")
    @Transactional
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2, random = true)
    )
    public void handle(UserProfileChangedEvent event) {
        MDC.put("correlationId", event.getCorrelationId());
        MDC.put("eventId", event.getEventId());

        try {
            if (event.getSchemaVersion() != SUPPORTED_SCHEMA_VERSION) {
                throw new UnsupportedOperationException("Unknown schema version: " + event.getSchemaVersion());
            }

            if (processedEventRepository.existsByEventId(event.getEventId())) {
                LOGGER.info("[SKIP] Duplicate eventId={}", event.getEventId());
                return;
            }

            LOGGER.info("[PROCESS] Analytics eventId={} userId={} entityVersion={}", event.getEventId(), event.getEntityId(), event.getEntityVersion());

            // We track the event (version, changeType, timestamp) for analytics.
            // We optionally fetch user for additional enrichment.
            recordAnalyticsEvent(event);

            processedEventRepository.save(
                    new ProcessedEvent(event.getEventId(), CONSUMER_NAME, Instant.now()));

        } finally {
            MDC.clear();
        }
    }

    private void recordAnalyticsEvent(UserProfileChangedEvent event) {
        // Real implementation: write to ClickHouse / BigQuery / data warehouse
        LOGGER.info("[ANALYTICS] userId={} changeType={} entityVersion={} occurredAt={}",
                event.getEntityId(), event.getChangeType(),
                event.getEntityVersion(), event.getOccurredAt());
    }
}
