package com.workshop.before.client;

import com.workshop.before.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * PROBLEM: A third tightly coupled synchronous call.
 * Three services = three points of failure in one user-update transaction.
 */
@Component
public class NotificationClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendInAppNotification(User user) {
        restTemplate.postForObject(
                "http://notification-service/api/in-app/profile",
                user,
                Void.class
        );
    }
}
