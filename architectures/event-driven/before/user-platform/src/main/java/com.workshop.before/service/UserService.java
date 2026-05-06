package com.workshop.before.service;

import com.workshop.before.client.*;
import com.workshop.before.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * PROBLEMS ALL IN ONE PLACE:
 *
 * 1. TIGHT COUPLING: UserService directly knows about Email, Analytics,
 *    Notification services. Adding "SmsService" means editing this class.
 *
 * 2. CASCADING FAILURE: if any downstream call throws, Spring rolls back
 *    the DB transaction — user profile update is lost even if it was valid.
 *
 * 3. THUNDERING HERD: all three downstream services are hit synchronously
 *    at the same instant.
 *
 * 4. NO IDEMPOTENCY: if the client retries the HTTP request, the user gets
 *    two emails, two analytics events, two notifications.
 *
 * 5. PII LEAKAGE: full User object (phone, address) is pushed to services
 *    that may only need the email address.
 *
 * 6. NO ORDERING CONTROL: concurrent updates can race; no version check.
 *
 * 7. LATENCY MULTIPLICATION: response time = sum of all downstream calls.
 */
@Service
public class UserService {

    private final UserRepository    userRepository;
    private final EmailClient       emailClient;
    private final AnalyticsClient   analyticsClient;
    private final NotificationClient notificationClient;

    public UserService(UserRepository userRepository,
                       EmailClient emailClient,
                       AnalyticsClient analyticsClient,
                       NotificationClient notificationClient) {
        this.userRepository      = userRepository;
        this.emailClient         = emailClient;
        this.analyticsClient     = analyticsClient;
        this.notificationClient  = notificationClient;
    }

    @Transactional
    public User updateProfile(String userId, Map<String, String> changes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (changes.containsKey("displayName")) {
            user.setDisplayName(changes.get("displayName"));
        }
        if (changes.containsKey("phone")) {
            user.setPhone(changes.get("phone"));
        }

        userRepository.save(user);

        // PROBLEM: these three calls happen inside the @Transactional scope.
        // If emailClient.notifyProfileChanged() throws a RuntimeException,
        // the entire transaction rolls back. The user's data is NOT saved.
        emailClient.notifyProfileChanged(user);       // may block 2 seconds
        analyticsClient.trackProfileUpdate(user);     // may block 3 seconds
        notificationClient.sendInAppNotification(user); // may block 1 second

        // PROBLEM: total latency for user = 2 + 3 + 1 = at least 6 seconds minimum
        return user;
    }
}
