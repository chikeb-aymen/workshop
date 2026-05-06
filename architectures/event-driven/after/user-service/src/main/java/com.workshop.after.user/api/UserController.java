package com.workshop.after.user.api;

import com.workshop.after.user.domain.User;
import com.workshop.after.user.service.UserDomainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserDomainService userDomainService;
    private final UserRepository userRepository;

    public UserController(UserDomainService userDomainService,
                          UserRepository userRepository) {
        this.userDomainService = userDomainService;
        this.userRepository = userRepository;
    }

    /**
     * Source of truth read endpoint — called by consumers (email-service,
     * analytics-service) after they receive a UserProfileChanged event.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable String userId) {
        return userRepository.findById(userId)
                .map(u -> ResponseEntity.ok(UserDto.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateProfile(
            @PathVariable String userId,
            @RequestBody Map<String, String> changes,
            @RequestHeader(value = "X-Correlation-Id", required = false)
            String correlationId) {

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        User saved = userDomainService.updateProfile(userId, changes, correlationId);
        return ResponseEntity.ok(UserDto.from(saved));
    }

    /**
     * PII / minimal exposure:
     * UserDto deliberately omits sensitive fields (phone, address).
     * Consumers receive only what they need via this endpoint.
     * If a consumer needs phone, it requests a specific, scoped endpoint
     * with explicit authorization.
     */
    public record UserDto(String id, String email, String displayName, long version) {
        public static UserDto from(User u) {
            return new UserDto(u.getId(), u.getEmail(), u.getDisplayName(), u.getVersion());
        }
    }
}
