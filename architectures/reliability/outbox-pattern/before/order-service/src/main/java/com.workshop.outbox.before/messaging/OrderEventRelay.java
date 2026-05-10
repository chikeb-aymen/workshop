package com.workshop.outbox.before.messaging;

import com.workshop.outbox.before.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * PROBLEM: this relay is a thin wrapper around rabbitTemplate.convertAndSend().
 * It runs after the DB transaction commits (AFTER_COMMIT phase), which prevents
 * ghost events. But it does NOT prevent silent event loss:
 *
 * Gap 1 — JVM killed between commit and listener execution:
 *   The listener never runs. The event is gone. No retry possible.
 *   This happens on every rolling deployment, OOM kill, and SIGKILL.
 *
 * Gap 2 — RabbitMQ unavailable when listener executes:
 *   rabbitTemplate.convertAndSend() throws AmqpException.
 *   The transaction is already committed. There is no retry mechanism here.
 *   Catch-and-Log is the only option — the event is permanently dropped.
 *
 * Gap 3 — No durable store:
 *   Spring ApplicationEvents live in memory. If the JVM restarts for any reason,
 *   all in-flight events vanish. There is no table to query, no metric to alert on.
 */
@Component
public class OrderEventRelay {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventRelay.class);

    private static final String EXCHANGE   = "orders.exchange";
    private static final String ROUTING_KEY = "order.placed";

    private final RabbitTemplate rabbitTemplate;

    public OrderEventRelay(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedApplicationEvent event) {
        Order order = event.getOrder();

        // PROBLEM: no event_id on the payload — consumers cannot deduplicate.
        // PROBLEM: full Order object sent — includes customer data not needed by InventoryService.
        // PROBLEM: if this throws, the event is silently lost (transaction already committed).
        Map<String, Object> payload = Map.of(
                "orderId",    order.getId(),
                "customerId", order.getCustomerId(),
                "status",     order.getStatus(),
                "totalAmount", order.getTotalAmount(),
                "placedAt",   order.getPlacedAt().toString()
        );

        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, payload);
            LOGGER.info("Published OrderPlaced event for order={}", order.getId());
        } catch (AmqpException ex) {
            // PROBLEM: swallowing this exception is the only option here —
            // the DB transaction is committed, there is nothing to roll back to.
            // This event is permanently lost. The log line is the only artifact.
            LOGGER.error("FAILED to publish OrderPlaced event for order={} — event is permanently lost: {}", order.getId(), ex.getMessage());
        }
    }
}
