package com.workshop.outbox.after.order.outbox;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * A row in the outbox_events table. Written in the same @Transactional call as the business entity.
 * The relay reads PENDING rows, publishes them to RabbitMQ, and deletes them on success.
 * Rows survive JVM restarts — that is the entire point.
 */
@Entity
@Table(
        name = "outbox_events",
        indexes = @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
)
public class OutboxEvent {

    @Id
    private String id;

    @Column(nullable = false)
    private String aggregateType;   // "Order"

    @Column(nullable = false)
    private String aggregateId;     // orderId

    @Column(nullable = false)
    private String eventType;       // "OrderPlaced"

    @Column(nullable = false)
    private String routingKey;      // "order.placed" — relay uses this verbatim

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;         // JSON-serialised OrderPlacedEvent

    @Column(nullable = false)
    private String status;          // PENDING | SENT, sometimes we can make sent boolean 0 or 1

    @Column(nullable = false)
    private Instant createdAt;

    private Instant sentAt;

    @Column(nullable = false)
    private int attempts;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    protected OutboxEvent() {
    }

    public OutboxEvent(String id, String aggregateType, String aggregateId, String eventType, String routingKey, String payload) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.routingKey = routingKey;
        this.payload = payload;
        this.status = "PENDING";
        this.createdAt = Instant.now();
        this.attempts = 0;
    }

    public void markSent() {
        this.status = "SENT";
        this.sentAt = Instant.now();
    }

    public void recordFailure(String error) {
        this.attempts++;
        this.lastError = error;
    }

    public String getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getPayload() {
        return payload;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getLastError() {
        return lastError;
    }
}
