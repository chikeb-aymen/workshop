package com.workshop.observer.listener;

import com.workshop.observer.event.OrderPlacedEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationListener {

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.println("[Email] Sending confirmation for order: " + event.getOrder().getId());
    }
}
