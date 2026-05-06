package com.workshop.before.client;

import com.workshop.before.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * PROBLEM: Another synchronous downstream call.
 * The user must wait for analytics to complete before getting a response.
 * Analytics data is irrelevant to the core user update transaction.
 */
@Component
public class AnalyticsClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public void trackProfileUpdate(User user) {
        // PROBLEM: no idempotency key — duplicate retries = duplicate analytics events
        // PROBLEM: analytics outage kills the user update flow
        restTemplate.postForObject(
                "http://analytics-service/api/events/user-updated",
                user,
                Void.class
        );
    }
}
