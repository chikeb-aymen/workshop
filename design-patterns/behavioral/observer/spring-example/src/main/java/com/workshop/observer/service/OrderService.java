package com.workshop.observer.service;

import com.workshop.observer.event.OrderPlacedEvent;
import com.workshop.observer.model.Order;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final ApplicationEventPublisher eventPublisher;

    public OrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public Order placeOrder(Order order) {
        System.out.println("[OrderService] Order placed: " + order.getId());
        eventPublisher.publishEvent(new OrderPlacedEvent(this, order));
        return order;
    }
}
