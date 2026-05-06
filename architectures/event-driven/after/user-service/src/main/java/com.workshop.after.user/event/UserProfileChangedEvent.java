package com.workshop.after.user.event;

import java.time.Instant;

public class UserProfileChangedEvent {

    // ---------- identity ----------
    private String  eventId;       // UUID, unique per emission
    private int     schemaVersion; // increment when shape changes

    // ---------- what ----------
    private String  eventType;     // "UserProfileChanged"
    private String  entityType;    // "User"
    private String  entityId;      // userId (opaque, no PII)
    private String  changeType;    // CREATED | UPDATED | DELETED

    // ---------- when ----------
    private Instant occurredAt;

    // ---------- version ----------
    private long    entityVersion; // monotonic counter on the User entity;
    // consumers can skip stale events

    // ---------- tracing ----------
    private String  correlationId; // from HTTP X-Correlation-Id header
    private String  traceId;       // from MDC / OpenTelemetry

    public UserProfileChangedEvent() {}

    public UserProfileChangedEvent(String eventId,
                                   int schemaVersion,
                                   String eventType,
                                   String entityType,
                                   String entityId,
                                   String changeType,
                                   Instant occurredAt,
                                   long entityVersion,
                                   String correlationId,
                                   String traceId) {
        this.eventId       = eventId;
        this.schemaVersion = schemaVersion;
        this.eventType     = eventType;
        this.entityType    = entityType;
        this.entityId      = entityId;
        this.changeType    = changeType;
        this.occurredAt    = occurredAt;
        this.entityVersion = entityVersion;
        this.correlationId = correlationId;
        this.traceId       = traceId;
    }

    public String  getEventId()       { return eventId; }
    public int     getSchemaVersion() { return schemaVersion; }
    public String  getEventType()     { return eventType; }
    public String  getEntityType()    { return entityType; }
    public String  getEntityId()      { return entityId; }
    public String  getChangeType()    { return changeType; }
    public Instant getOccurredAt()    { return occurredAt; }
    public long    getEntityVersion() { return entityVersion; }
    public String  getCorrelationId() { return correlationId; }
    public String  getTraceId()       { return traceId; }
}
