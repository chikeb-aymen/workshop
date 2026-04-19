package com.workshop.observer.listener;

import com.workshop.observer.event.OrderPlacedEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryListener {

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.println("[Inventory] Reducing stock for: " + event.getOrder().getProduct()
                + " (qty: " + event.getOrder().getQuantity() + ")");
    }
}
