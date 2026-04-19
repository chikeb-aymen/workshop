package com.workshop.observer.listener;

import com.workshop.observer.event.OrderPlacedEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsListener {

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.println("[Analytics] Logging order event: " + event.getOrder().getId());
    }
}
