package com.workshop.after.email.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

/**
 * Clear ownership + fetch-back:
 * EmailService never holds a copy of user data. It fetches from the
 * source of truth (UserService) only when it needs to process an event.
 * <p>
 * Resilience:
 * 404 is handled gracefully (user deleted between event emission and fetch).
 * Network errors bubble up so the @RabbitListener can retry via Spring Retry.
 * <p>
 * Avoid callback storms:
 * In production, add a circuit breaker (Resilience4j) and a short read cache
 * (Caffeine, TTL ~5s) here to prevent hammering UserService under load.
 */
@Component
public class UserServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${user.service.base-url}")
    private String baseUrl;

    public Optional<UserDto> fetchUser(String userId, String correlationId) {
        String url = baseUrl + "/api/users/" + userId;
        try {
            ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            // User deleted after event was emitted — safe to skip
            LOGGER.warn("[FETCH] User not found userId={} correlationId={}", userId, correlationId);
            return Optional.empty();
        }
    }

    public record UserDto(String id, String email, String displayName, long version) {
    }
}
