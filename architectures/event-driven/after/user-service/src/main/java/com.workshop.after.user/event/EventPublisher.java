package com.workshop.after.user.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;


@Component
public class EventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisher.class);

    private static final String EXCHANGE = "user.events";
    private static final String ROUTING_KEY = "user.profile.changed";
    private static final int SCHEMA_VERSION = 1;

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserProfileChanged(String userId,
                                          long entityVersion,
                                          String changeType,
                                          String correlationId) {
        UserProfileChangedEvent event = new UserProfileChangedEvent(
                UUID.randomUUID().toString(),
                SCHEMA_VERSION,
                "UserProfileChanged",
                "User",
                userId,
                changeType,
                Instant.now(),
                entityVersion,
                correlationId,
                "trace-" + UUID.randomUUID()   // replace with real OTel trace id
        );

        LOGGER.info("[PUBLISH] eventId={} userId={} correlationId={} version={}", event.getEventId(), userId, correlationId, entityVersion);

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
}
