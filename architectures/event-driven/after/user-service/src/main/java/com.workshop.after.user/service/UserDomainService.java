package com.workshop.after.user.service;

import com.workshop.after.user.domain.User;
import com.workshop.after.user.domain.UserRepository;
import com.workshop.after.user.event.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserDomainService {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public UserDomainService(UserRepository userRepository,
                             EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public User updateProfile(String userId,
                              Map<String, String> changes,
                              String correlationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (changes.containsKey("displayName")) {
            user.setDisplayName(changes.get("displayName"));
        }
        if (changes.containsKey("phone")) {
            user.setPhone(changes.get("phone"));
        }

        User saved = userRepository.save(user);

        // Publish AFTER save so consumers can fetch consistent state.
        // entityVersion (JPA @Version) is incremented by save — included
        // in the event so consumers can detect stale/duplicate events.
        eventPublisher.publishUserProfileChanged(
                saved.getId(),
                saved.getVersion(),
                "UPDATED",
                correlationId
        );

        return saved;
    }
}
