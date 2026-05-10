package com.workshop.outbox.after.fulfillment.consumer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Local representation of the OrderPlaced event emitted by order-service.
 *
 * Each consumer service defines its own copy of the event DTO — this is intentional.
 * Services are independently deployable units; sharing a compiled JAR would reintroduce
 * build-time coupling between teams. The contract is enforced through schemaVersion:
 * consumers must reject versions they do not recognise rather than silently misprocess them.
 *
 * fulfillment-service needs: aggregateId (orderId), customerId, totalItems count.
 * It does not need unitPrice — that field is ignored even though it arrives in the payload.
 * Jackson deserializes only the fields declared here; unknown fields are silently skipped.
 */
public class OrderPlacedEvent {

    // ---------- identity ----------
    private String  eventId;          // UUID — used as idempotency key in processed_events
    private int     schemaVersion;    // consumers reject unknown versions to DLQ

    // ---------- what ----------
    private String  eventType;        // "OrderPlaced"
    private String  aggregateId;      // orderId
    private long    aggregateVersion; // JPA @Version value at time of write

    // ---------- when ----------
    private Instant occurredAt;

    // ---------- business data ----------
    private String         customerId;
    private BigDecimal     totalAmount;
    private List<ItemDto>  items;     // quantity needed to compute totalItems for the packing job

    public OrderPlacedEvent() {}

    public String         getEventId()          { return eventId; }
    public int            getSchemaVersion()     { return schemaVersion; }
    public String         getEventType()         { return eventType; }
    public String         getAggregateId()       { return aggregateId; }
    public long           getAggregateVersion()  { return aggregateVersion; }
    public Instant        getOccurredAt()        { return occurredAt; }
    public String         getCustomerId()        { return customerId; }
    public BigDecimal     getTotalAmount()       { return totalAmount; }
    public List<ItemDto>  getItems()             { return items; }

    public record ItemDto(String skuId, int quantity) {}
}
