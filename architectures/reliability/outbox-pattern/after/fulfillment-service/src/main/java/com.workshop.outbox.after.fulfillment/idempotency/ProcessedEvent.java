package com.workshop.outbox.after.fulfillment.idempotency;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    private String eventId;

    @Column(nullable = false)
    private String consumer;

    @Column(nullable = false)
    private Instant processedAt;

    protected ProcessedEvent() {}

    public ProcessedEvent(String eventId, String consumer) {
        this.eventId     = eventId;
        this.consumer    = consumer;
        this.processedAt = Instant.now();
    }

    public String  getEventId()     { return eventId; }
    public String  getConsumer()    { return consumer; }
    public Instant getProcessedAt() { return processedAt; }
}
