package com.workshop.outbox.after.order.outbox;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Rows that failed MAX_ATTEMPTS publish attempts are moved here.
 * The relay skips them and continues — poison messages cannot block the relay.
 * Ops can inspect this table to diagnose and replay manually.
 */
@Entity
@Table(name = "outbox_dead_letter")
public class OutboxDeadLetter {

    @Id
    private String id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Instant failedAt;

    @Column(nullable = false)
    private int totalAttempts;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    protected OutboxDeadLetter() {}

    public OutboxDeadLetter(OutboxEvent event) {
        this.id            = event.getId();
        this.aggregateType = event.getAggregateType();
        this.aggregateId   = event.getAggregateId();
        this.eventType     = event.getEventType();
        this.payload       = event.getPayload();
        this.failedAt      = Instant.now();
        this.totalAttempts = event.getAttempts();
        this.lastError     = event.getLastError();
    }

    public String  getId()            { return id; }
    public String  getAggregateId()   { return aggregateId; }
    public String  getEventType()     { return eventType; }
    public String  getPayload()       { return payload; }
    public Instant getFailedAt()      { return failedAt; }
    public int     getTotalAttempts() { return totalAttempts; }
    public String  getLastError()     { return lastError; }
}
