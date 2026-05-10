package com.workshop.outbox.after.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String skuId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    protected OrderItem() {}

    public OrderItem(String skuId, int quantity, BigDecimal unitPrice) {
        this.skuId     = skuId;
        this.quantity  = quantity;
        this.unitPrice = unitPrice;
    }

    public String     getSkuId()     { return skuId; }
    public int        getQuantity()  { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
