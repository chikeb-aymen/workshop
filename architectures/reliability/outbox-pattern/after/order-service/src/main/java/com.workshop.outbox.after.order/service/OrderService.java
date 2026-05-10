package com.workshop.outbox.after.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workshop.outbox.after.order.api.PlaceOrderRequest;
import com.workshop.outbox.after.order.domain.Order;
import com.workshop.outbox.after.order.domain.OrderItem;
import com.workshop.outbox.after.order.domain.OrderRepository;
import com.workshop.outbox.after.order.event.OrderPlacedEvent;
import com.workshop.outbox.after.order.outbox.OutboxEvent;
import com.workshop.outbox.after.order.outbox.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * FIX: zero AMQP imports. No RabbitTemplate. No ApplicationEventPublisher.
 * The only external contract is the DB: two inserts, one transaction.
 *
 * If this transaction commits   → Order + OutboxEvent both exist. Relay delivers the event.
 * If this transaction rolls back → Neither row exists. No ghost event. No lost event.
 * If the JVM dies after commit  → OutboxEvent row survives in the DB. Relay picks it up on restart.
 */
@Service
public class OrderService {

    private final OrderRepository       orderRepository;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper          objectMapper;

    public OrderService(OrderRepository orderRepository, OutboxEventRepository outboxRepo, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxRepo      = outboxRepo;
        this.objectMapper    = objectMapper;
    }

    @Transactional
    public Order placeOrder(PlaceOrderRequest request) throws Exception {
        List<OrderItem> items = request.items().stream()
                .map(i -> new OrderItem(i.skuId(), i.quantity(), i.unitPrice()))
                .toList();

        Order order = new Order(UUID.randomUUID().toString(), request.customerId(), items);
        orderRepository.save(order);

        // FIX: outbox row written in the SAME @Transactional as the Order.
        // Atomicity is provided by the local DB transaction, not by a distributed protocol.
        OutboxEvent outboxEvent = buildOutboxEvent(order);
        outboxRepo.save(outboxEvent);

        return order;
    }

    public Optional<Order> findById(String orderId) {
        return orderRepository.findById(orderId);
    }

    private OutboxEvent buildOutboxEvent(Order order) throws Exception {
        List<OrderPlacedEvent.ItemDto> itemDtos = order.getItems().stream()
                .map(i -> new OrderPlacedEvent.ItemDto(i.getSkuId(), i.getQuantity()))
                .toList();

        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID().toString(),
                1,
                order.getId(),
                order.getVersion() != null ? order.getVersion() : 0L,
                order.getPlacedAt(),
                order.getCustomerId(),
                order.getTotalAmount(),
                itemDtos
        );

        return new OutboxEvent(
                UUID.randomUUID().toString(),
                "Order",
                order.getId(),
                "OrderPlaced",
                "order.placed",
                objectMapper.writeValueAsString(event)
        );
    }
}
