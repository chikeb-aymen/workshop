package com.workshop.after.analytics.idempotency;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Each consumer maintains its own processed_events table.
 * Before processing, we check if eventId was already processed.
 * After processing, we insert a row.
 * Both in one transaction → exactly-once processing even with at-least-once delivery.
 */
@Entity
@Table(name = "processed_events",
        uniqueConstraints = @UniqueConstraint(columnNames = "event_id"))
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "consumer_name", nullable = false)
    private String consumerName;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public ProcessedEvent() {
    }

    public ProcessedEvent(String eventId, String consumerName, Instant processedAt) {
        this.eventId = eventId;
        this.consumerName = consumerName;
        this.processedAt = processedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
