package com.workshop.outbox.after.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private String id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Instant placedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    @Version
    private Long version;

    protected Order() {}

    public Order(String id, String customerId, List<OrderItem> items) {
        this.id          = id;
        this.customerId  = customerId;
        this.items       = items;
        this.status      = "PLACED";
        this.totalAmount = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.placedAt    = Instant.now();
    }

    public String         getId()          { return id; }
    public String         getCustomerId()  { return customerId; }
    public String         getStatus()      { return status; }
    public BigDecimal     getTotalAmount() { return totalAmount; }
    public Instant        getPlacedAt()    { return placedAt; }
    public List<OrderItem> getItems()      { return items; }
    public Long           getVersion()     { return version; }
}
