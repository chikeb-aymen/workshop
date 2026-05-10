package com.workshop.outbox.after.fulfillment.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "packing_jobs")
public class PackingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private int totalItems;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    protected PackingJob() {}

    public PackingJob(String orderId, String customerId, int totalItems) {
        this.orderId    = orderId;
        this.customerId = customerId;
        this.totalItems = totalItems;
        this.status     = "PENDING";
        this.createdAt  = Instant.now();
    }

    public Long    getId()         { return id; }
    public String  getOrderId()    { return orderId; }
    public String  getCustomerId() { return customerId; }
    public int     getTotalItems() { return totalItems; }
    public String  getStatus()     { return status; }
    public Instant getCreatedAt()  { return createdAt; }
}
