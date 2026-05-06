package com.workshop.before.api;

import com.workshop.before.domain.User;
import com.workshop.before.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<User> updateProfile(
            @PathVariable String userId,
            @RequestBody Map<String, String> changes) {
        // PROBLEM: if any downstream is slow, this HTTP call hangs
        // PROBLEM: no idempotency key on the request
        User updated = userService.updateProfile(userId, changes);
        return ResponseEntity.ok(updated);
    }
}
