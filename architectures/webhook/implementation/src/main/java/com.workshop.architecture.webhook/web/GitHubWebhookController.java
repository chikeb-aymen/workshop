package com.workshop.architecture.webhook.web;

import com.workshop.architecture.webhook.ingest.GitHubWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/integrations/github")
public class GitHubWebhookController {

    private final GitHubWebhookService gitHubWebhookService;

    public GitHubWebhookController(GitHubWebhookService gitHubWebhookService) {
        this.gitHubWebhookService = gitHubWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receive(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature256,
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventName) {

        if (signature256 == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing X-Hub-Signature-256");
        }

        try {
            gitHubWebhookService.handleGitHubDelivery(
                    eventName != null ? eventName : "push",
                    rawBody,
                    signature256);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad signature");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Bad payload");
        }

        return ResponseEntity.ok("accepted");
    }
}
