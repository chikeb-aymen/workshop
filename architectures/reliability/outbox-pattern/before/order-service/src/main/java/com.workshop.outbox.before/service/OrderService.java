package com.workshop.outbox.before.service;

import com.workshop.outbox.before.api.PlaceOrderRequest;
import com.workshop.outbox.before.domain.Order;
import com.workshop.outbox.before.domain.OrderItem;
import com.workshop.outbox.before.domain.OrderRepository;
import com.workshop.outbox.before.messaging.OrderPlacedApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * PROBLEMS:
 * <p>
 * 1. SILENT EVENT LOSS — the transaction commits, then the JVM is killed before the
 *
 * @TransactionalEventListener fires. The order is in the DB. The event is never published.
 * No error. No log entry. No record that the event was supposed to be emitted.
 * This happens on every rolling deployment, OOM kill, and forced pod eviction.
 * <p>
 * 2. BROKER FAILURE = PERMANENT LOSS — if RabbitMQ is unreachable when the listener fires,
 * rabbitTemplate.convertAndSend() throws AmqpException. The DB transaction is already
 * committed. There is nothing to retry against. The event is permanently dropped.
 * <p>
 * 3. NO AUDIT TRAIL — there is no durable record that event X was committed and needs delivery.
 * Support cannot verify whether an event was emitted for a given order without reading broker
 * logs, which are ephemeral and not always available.
 * <p>
 * 4. NO IDEMPOTENCY — if the broker re-delivers the event (network blip, consumer restart),
 * InventoryService decrements stock twice. FulfillmentService creates two packing jobs.
 * <p>
 * 5. EXCEPTION LEAKAGE — AmqpException from the listener can propagate back to the HTTP thread
 * even though the order was already committed. The customer sees a 500 and retries,
 * creating a duplicate order.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Order placeOrder(PlaceOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(i -> new OrderItem(i.skuId(), i.quantity(), i.unitPrice()))
                .toList();

        Order order = new Order(UUID.randomUUID().toString(), request.customerId(), items);
        orderRepository.save(order);

        // PROBLEM: this fires a Spring ApplicationEvent that is handled by
        // OrderEventRelay with @TransactionalEventListener(phase = AFTER_COMMIT).
        //
        // This solves ghost events (event fires only after commit) but does NOT
        // solve silent loss:
        //   - if the JVM is killed between commit and the listener executing → event lost
        //   - if rabbitTemplate.send() throws inside the listener → event lost
        //   - there is no durable record that this event needs to be delivered
        eventPublisher.publishEvent(new OrderPlacedApplicationEvent(this, order));

        return order;
    }

    public Optional<Order> findById(String orderId) {
        return orderRepository.findById(orderId);
    }
}
