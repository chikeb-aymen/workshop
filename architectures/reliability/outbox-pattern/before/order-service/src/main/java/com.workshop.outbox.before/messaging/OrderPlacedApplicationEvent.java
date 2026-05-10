package com.workshop.outbox.before.messaging;

import com.workshop.outbox.before.domain.Order;
import org.springframework.context.ApplicationEvent;

/**
 * An in-process Spring ApplicationEvent. This is NOT a durable record.
 * If the JVM dies after the transaction commits but before this event is
 * processed, it is gone — there is no persistent store backing it.
 */
public class OrderPlacedApplicationEvent extends ApplicationEvent {

    private final Order order;

    public OrderPlacedApplicationEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
