package com.workshop.outbox.after.inventory.consumer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Local representation of the OrderPlaced event emitted by order-service.
 *
 * Each consumer service owns its own copy of the event DTO — this is intentional.
 * Services are independent deployable units; sharing a compiled JAR would create
 * a build-time coupling that defeats the purpose of independent deployment.
 *
 * In production, you would enforce the contract through a schema registry
 * (Avro + Confluent Schema Registry, or Protobuf). For this workshop, the
 * shared contract is documented in schemaVersion: consumers must reject any
 * version they do not recognise rather than silently misprocess it.
 */
public class OrderPlacedEvent {

    // ---------- identity ----------
    private String  eventId;        // UUID — used as idempotency key in processed_events
    private int     schemaVersion;  // consumers reject unknown versions to DLQ

    // ---------- what ----------
    private String  eventType;      // "OrderPlaced"
    private String  aggregateId;    // orderId
    private long    aggregateVersion; // JPA @Version value at time of write

    // ---------- when ----------
    private Instant occurredAt;

    // ---------- business data ----------
    private String         customerId;
    private BigDecimal     totalAmount;
    private List<ItemDto>  items;   // skuId + quantity needed to reserve stock

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
