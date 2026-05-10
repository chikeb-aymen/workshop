package com.workshop.outbox.after.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderPlacedEvent {

    private String eventId;        // UUID, unique per emission — consumers use this for idempotency
    private int schemaVersion;  // increment when shape changes; consumers reject unknown versions

    private String eventType;      // "OrderPlaced"
    private String aggregateId;    // orderId
    private long aggregateVersion; // JPA @Version — consumers can detect stale re-deliveries

    private Instant occurredAt;

    private String customerId;
    private BigDecimal totalAmount;
    private List<ItemDto> items;

    public OrderPlacedEvent() {
    }

    public OrderPlacedEvent(String eventId,
                            int schemaVersion,
                            String aggregateId,
                            long aggregateVersion,
                            Instant occurredAt,
                            String customerId,
                            BigDecimal totalAmount,
                            List<ItemDto> items) {
        this.eventId = eventId;
        this.schemaVersion = schemaVersion;
        this.eventType = "OrderPlaced";
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.occurredAt = occurredAt;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public String getEventId() {
        return eventId;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public long getAggregateVersion() {
        return aggregateVersion;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public record ItemDto(String skuId, int quantity) {
    }
}
