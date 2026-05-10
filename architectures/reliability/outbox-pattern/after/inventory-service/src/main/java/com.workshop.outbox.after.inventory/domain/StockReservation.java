package com.workshop.outbox.after.inventory.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "stock_reservations")
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String skuId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Instant reservedAt;

    protected StockReservation() {}

    public StockReservation(String orderId, String skuId, int quantity) {
        this.orderId    = orderId;
        this.skuId      = skuId;
        this.quantity   = quantity;
        this.reservedAt = Instant.now();
    }

    public Long    getId()         { return id; }
    public String  getOrderId()    { return orderId; }
    public String  getSkuId()      { return skuId; }
    public int     getQuantity()   { return quantity; }
    public Instant getReservedAt() { return reservedAt; }
}
