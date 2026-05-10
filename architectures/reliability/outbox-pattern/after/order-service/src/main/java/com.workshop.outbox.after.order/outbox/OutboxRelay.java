package com.workshop.outbox.after.order.outbox;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls outbox_events every 500ms and delegates each row to OutboxPublisher.
 *
 * FIX: runs entirely outside the HTTP request path — broker failures never surface to the caller.
 * FIX: pending_count gauge enables alerting when the backlog grows unexpectedly.
 *
 * Production note: in a multi-instance deployment, add a distributed lock (e.g. ShedLock)
 * or use SELECT ... FOR UPDATE on the findPending query to avoid duplicate relay attempts
 * across pods. At-least-once delivery (handled by consumer idempotency) is still correct without it,
 * but a lock reduces unnecessary duplicate publishes.
 */
@Component
public class OutboxRelay {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventRepository outboxRepo;
    private final OutboxPublisher       publisher;

    public OutboxRelay(OutboxEventRepository outboxRepo, OutboxPublisher publisher, MeterRegistry meterRegistry) {
        this.outboxRepo = outboxRepo;
        this.publisher  = publisher;

        // FIX: live gauge — reflects current count on every Prometheus scrape.
        // Alert when outbox.pending_count > 100 for more than 2 minutes.
        meterRegistry.gauge("outbox.pending_count", outboxRepo,
                repo -> repo.countByStatus("PENDING"));
    }

    @Scheduled(fixedDelay = 500)
    public void relay() {
        // Rows fetched in createdAt ASC order to preserve per-aggregate event ordering.
        // Or find by sent 0 in case of boolean
        outboxRepo.findPendingOrderedByCreatedAt()
                .forEach(publisher::publishSingle);
    }
}
