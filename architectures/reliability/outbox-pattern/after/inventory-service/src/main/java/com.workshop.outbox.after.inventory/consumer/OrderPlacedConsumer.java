package com.workshop.outbox.after.inventory.consumer;

import com.workshop.outbox.after.inventory.domain.StockReservation;
import com.workshop.outbox.after.inventory.domain.StockReservationRepository;
import com.workshop.outbox.after.inventory.idempotency.ProcessedEvent;
import com.workshop.outbox.after.inventory.idempotency.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotency:
 *   existsByEventId check + processed_events insert in the same @Transactional as the reservation.
 *   If the broker re-delivers the same event (relay's at-least-once guarantee), the second
 *   delivery is detected and skipped — no double stock reservation.
 *
 * Schema evolution:
 *   Unknown schemaVersion is rejected immediately with an exception. Spring AMQP routes the
 *   message to the DLQ (setDefaultRequeueRejected=false in RabbitConfig) rather than retrying
 *   forever or silently misprocessing a field that was renamed or removed.
 *   Bump SUPPORTED_SCHEMA_VERSION when the OrderPlacedEvent shape changes.
 *
 * Resilience:
 *   @Retryable catches transient errors (DB blip, network hiccup) and retries with exponential
 *   backoff. After maxAttempts, Spring AMQP routes the message to the DLQ.
 *
 * Atomicity of business action + idempotency mark:
 *   processed_events is inserted in the SAME @Transactional as StockReservation inserts.
 *   If the transaction rolls back (e.g. DB constraint), the idempotency row also rolls back
 *   and the event will be retried correctly — no partial reservation with no idempotency record.
 */
@Component
public class OrderPlacedConsumer {

    private static final Logger LOGGER                   = LoggerFactory.getLogger(OrderPlacedConsumer.class);
    private static final int    SUPPORTED_SCHEMA_VERSION = 1;
    private static final String CONSUMER_NAME            = "inventory-service";

    private final StockReservationRepository reservationRepo;
    private final ProcessedEventRepository   processedEventRepo;

    public OrderPlacedConsumer(StockReservationRepository reservationRepo, ProcessedEventRepository processedEventRepo) {
        this.reservationRepo   = reservationRepo;
        this.processedEventRepo = processedEventRepo;
    }

    @RabbitListener(queues = "inventory-service.order.placed")
    @Transactional
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    public void handle(OrderPlacedEvent event) {
        // Schema version guard: reject unknown versions explicitly rather than silently misprocessing.
        // This event routes to the DLQ for manual inspection instead of being retried forever.
        if (event.getSchemaVersion() != SUPPORTED_SCHEMA_VERSION) {
            LOGGER.error("[SKIP] Unknown schemaVersion={} eventId={} — routing to DLQ", event.getSchemaVersion(), event.getEventId());
            throw new UnsupportedOperationException("Unsupported schema version: " + event.getSchemaVersion());
        }

        // Idempotency guard: if this event was already processed, skip without error.
        // Triggered when: relay re-delivers after a crash between publish and delete,
        // or the consumer itself restarts mid-processing and the broker re-delivers.
        if (processedEventRepo.existsByEventId(event.getEventId())) {
            LOGGER.info("[SKIP] Duplicate eventId={} — already processed by {}", event.getEventId(), CONSUMER_NAME);
            return;
        }

        LOGGER.info("[PROCESS] eventId={} orderId={} items={} aggregateVersion={}", event.getEventId(), event.getAggregateId(), event.getItems().size(), event.getAggregateVersion());

        // Business logic: create one reservation row per line item.
        for (OrderPlacedEvent.ItemDto item : event.getItems()) {
            reservationRepo.save(new StockReservation(
                    event.getAggregateId(),
                    item.skuId(),
                    item.quantity()
            ));
            LOGGER.info("[RESERVE] orderId={} skuId={} qty={}", event.getAggregateId(), item.skuId(), item.quantity());
        }

        // Idempotency mark: written in the SAME @Transactional as the reservations above.
        // If the DB rolls back, this row rolls back too — the event will be retried correctly.
        processedEventRepo.save(new ProcessedEvent(event.getEventId(), CONSUMER_NAME));

        LOGGER.info("[DONE] Reserved stock for orderId={} eventId={}", event.getAggregateId(), event.getEventId());
    }
}
