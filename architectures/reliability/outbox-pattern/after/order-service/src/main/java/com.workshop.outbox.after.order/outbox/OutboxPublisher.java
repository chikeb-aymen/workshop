package com.workshop.outbox.after.order.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workshop.outbox.after.order.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles a single outbox row: deserialises the payload, publishes to RabbitMQ,
 * and updates the row in the same @Transactional scope.
 *
 * Separated from OutboxRelay so that @Transactional is applied via Spring proxy
 * (self-invocation does not work with Spring AOP).
 *
 * At-least-once guarantee:
 *   If the JVM crashes after rabbitTemplate.send() succeeds but before the DB delete commits,
 *   the row is re-delivered on restart. Consumers guard against this with processed_events.
 */
@Component
public class OutboxPublisher {

    private static final Logger LOGGER       = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final String EXCHANGE     = "orders.exchange";
    private static final int    MAX_ATTEMPTS = 5;

    private final OutboxEventRepository      outboxRepo;
    private final OutboxDeadLetterRepository deadLetterRepo;
    private final RabbitTemplate             rabbitTemplate;
    private final ObjectMapper               objectMapper;

    public OutboxPublisher(OutboxEventRepository outboxRepo,
                           OutboxDeadLetterRepository deadLetterRepo,
                           RabbitTemplate rabbitTemplate,
                           ObjectMapper objectMapper) {
        this.outboxRepo     = outboxRepo;
        this.deadLetterRepo = deadLetterRepo;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper   = objectMapper;
    }

    @Transactional
    public void publishSingle(OutboxEvent event) {
        try {
            OrderPlacedEvent payload = objectMapper.readValue(event.getPayload(), OrderPlacedEvent.class);

            // rabbitTemplate.send() is NOT part of the DB transaction — it is intentionally outside.
            // The @Transactional here covers only the subsequent DB delete / save operations.
            rabbitTemplate.convertAndSend(EXCHANGE, event.getRoutingKey(), payload);

            // FIX: delete after confirmed send or make it to 1 in case of boolean
            outboxRepo.delete(event);

            LOGGER.info("[RELAY] Published eventType={} aggregateId={} attempts={}", event.getEventType(), event.getAggregateId(), event.getAttempts() + 1);

        } catch (AmqpException | Exception ex) {
            event.recordFailure(ex.getMessage());

            LOGGER.warn("[RELAY] Failed to publish eventType={} aggregateId={} attempts={}: {}", event.getEventType(), event.getAggregateId(), event.getAttempts(), ex.getMessage());

            if (event.getAttempts() >= MAX_ATTEMPTS) {
                // FIX: poison message handling — move to dead letter, never block the relay.
                deadLetterRepo.save(new OutboxDeadLetter(event));
                outboxRepo.delete(event);
                LOGGER.error("[RELAY] Moved to dead letter eventType={} aggregateId={} after {} attempts", event.getEventType(), event.getAggregateId(), event.getAttempts());
            } else {
                outboxRepo.save(event);
            }
        }
    }
}
