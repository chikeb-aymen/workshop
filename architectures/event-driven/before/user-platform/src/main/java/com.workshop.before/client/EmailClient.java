package com.workshop.before.client;

import com.workshop.before.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * PROBLEM: UserService is tightly coupled to EmailService.
 * If EmailService is down or slow, the user update blocks and fails.
 * Adding new services means editing UserService every time.
 */
@Component
public class EmailClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public void notifyProfileChanged(User user) {
        // PROBLEM: full User object (with PII) is sent to a downstream service
        // PROBLEM: no timeout, no retry, no fallback
        // PROBLEM: a 500 from EmailService rolls back the user update
        restTemplate.postForObject(
                "http://email-service/api/notify/profile-changed",
                user,          // sending phone, address, email — all PII
                Void.class
        );
    }
}
