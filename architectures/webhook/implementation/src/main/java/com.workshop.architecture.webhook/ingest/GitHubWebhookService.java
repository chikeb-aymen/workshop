package com.workshop.architecture.webhook.ingest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workshop.architecture.webhook.ci.PushFanOutDispatcher;
import com.workshop.architecture.webhook.config.AppProperties;
import com.workshop.architecture.webhook.github.GitHubSignatureVerifier;
import com.workshop.architecture.webhook.model.PushNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GitHubWebhookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubWebhookService.class);

    private final GitHubSignatureVerifier signatureVerifier;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final PushFanOutDispatcher fanOutDispatcher;

    public GitHubWebhookService(
            GitHubSignatureVerifier signatureVerifier,
            AppProperties appProperties,
            ObjectMapper objectMapper,
            PushFanOutDispatcher fanOutDispatcher) {
        this.signatureVerifier = signatureVerifier;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.fanOutDispatcher = fanOutDispatcher;
    }

    public void handleGitHubDelivery(String eventName, byte[] rawBody, String signature256) {
        String secret = appProperties.getGithub().getWebhookSecret();
        if (!signatureVerifier.verify(secret, rawBody, signature256)) {
            throw new SecurityException("Invalid X-Hub-Signature-256");
        }

        if (!"push".equalsIgnoreCase(eventName)) {
            LOGGER.info("Ignoring GitHub event type={}, only accept push event", eventName);
            return;
        }

        try {
            PushNotification push = objectMapper.readValue(rawBody, PushNotification.class);
            LOGGER.info("Verified push ref={} sha={} repo={}", push.getRef(), push.resolveSha(), push.resolveFullName());
            fanOutDispatcher.dispatchVerifiedPush(rawBody, push);
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed push payload", e);
        }
    }
}
