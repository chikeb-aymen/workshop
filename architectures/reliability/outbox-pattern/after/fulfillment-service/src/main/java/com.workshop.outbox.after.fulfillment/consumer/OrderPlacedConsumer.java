package com.workshop.outbox.after.fulfillment.consumer;

import com.workshop.outbox.after.fulfillment.domain.PackingJob;
import com.workshop.outbox.after.fulfillment.domain.PackingJobRepository;
import com.workshop.outbox.after.fulfillment.idempotency.ProcessedEvent;
import com.workshop.outbox.after.fulfillment.idempotency.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Second independent consumer — same idempotency and schema-guard pattern as inventory-service,
 * different business logic: creates a packing job for the warehouse.
 *
 * Independence guarantee:
 *   This service binds its own queue to the same exchange. Events for this queue accumulate
 *   independently of inventory-service's queue. Stopping inventory-service has zero effect here.
 *   Stopping this service has zero effect on inventory-service or order-service.
 *
 * Idempotency:
 *   processed_events insert is in the same @Transactional as the PackingJob insert.
 *   If the transaction rolls back, neither row exists — the event will be retried correctly.
 *   If a duplicate event arrives, existsByEventId() returns true and we skip without error.
 *
 * Schema evolution:
 *   Unknown schemaVersion is rejected immediately — routes to DLQ.
 *   Bump SUPPORTED_SCHEMA_VERSION when the OrderPlacedEvent shape changes.
 */
@Component
public class OrderPlacedConsumer {

    private static final Logger LOGGER                   = LoggerFactory.getLogger(OrderPlacedConsumer.class);
    private static final int    SUPPORTED_SCHEMA_VERSION = 1;
    private static final String CONSUMER_NAME            = "fulfillment-service";

    private final PackingJobRepository     packingJobRepo;
    private final ProcessedEventRepository processedEventRepo;

    public OrderPlacedConsumer(PackingJobRepository packingJobRepo, ProcessedEventRepository processedEventRepo) {
        this.packingJobRepo    = packingJobRepo;
        this.processedEventRepo = processedEventRepo;
    }

    @RabbitListener(queues = "fulfillment-service.order.placed")
    @Transactional
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    public void handle(OrderPlacedEvent event) {
        // Schema version guard — reject unknown versions to DLQ rather than silently misprocessing.
        if (event.getSchemaVersion() != SUPPORTED_SCHEMA_VERSION) {
            LOGGER.error("[SKIP] Unknown schemaVersion={} eventId={} — routing to DLQ", event.getSchemaVersion(), event.getEventId());
            throw new UnsupportedOperationException("Unsupported schema version: " + event.getSchemaVersion());
        }

        // Idempotency guard — skip if already processed (relay at-least-once re-delivery).
        if (processedEventRepo.existsByEventId(event.getEventId())) {
            LOGGER.info("[SKIP] Duplicate eventId={} — already processed by {}", event.getEventId(), CONSUMER_NAME);
            return;
        }

        LOGGER.info("[PROCESS] eventId={} orderId={} customerId={} aggregateVersion={}", event.getEventId(), event.getAggregateId(), event.getCustomerId(), event.getAggregateVersion());

        // Business logic: create one packing job per order.
        int totalItems = event.getItems().stream()
                .mapToInt(OrderPlacedEvent.ItemDto::quantity)
                .sum();

        PackingJob job = new PackingJob(
                event.getAggregateId(),
                event.getCustomerId(),
                totalItems
        );
        packingJobRepo.save(job);

        LOGGER.info("[PACKING] Created PackingJob id={} orderId={} totalItems={}", job.getId(), event.getAggregateId(), totalItems);

        // Idempotency mark in the SAME @Transactional as the PackingJob insert.
        processedEventRepo.save(new ProcessedEvent(event.getEventId(), CONSUMER_NAME));

        LOGGER.info("[DONE] PackingJob created for orderId={} eventId={}", event.getAggregateId(), event.getEventId());
    }
}
