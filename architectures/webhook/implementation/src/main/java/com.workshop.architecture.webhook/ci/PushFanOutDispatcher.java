package com.workshop.architecture.webhook.ci;

import com.workshop.architecture.webhook.github.GitHubSignatureVerifier;
import com.workshop.architecture.webhook.model.PipelineProviderConfig;
import com.workshop.architecture.webhook.model.PushNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@Service
public class PushFanOutDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushFanOutDispatcher.class);

    private final PipelineProviderRegistry registry;

    private final RestClient restClient;

    private final GitHubSignatureVerifier signatureVerifier;

    public PushFanOutDispatcher(PipelineProviderRegistry registry, RestClient restClient, GitHubSignatureVerifier signatureVerifier) {
        this.registry = registry;
        this.restClient = restClient;
        this.signatureVerifier = signatureVerifier;
    }

    public void dispatchVerifiedPush(byte[] rawBody, PushNotification parsed) {
        for (PipelineProviderConfig provider : registry.snapshot()) {
            try {
                String statusContext = provider.getLabel() != null && !provider.getLabel().isBlank()
                        ? provider.getLabel()
                        : "ci/" + provider.getId();

                var spec = restClient.post()
                        .uri(provider.getInboundWebhookUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "push")
                        .header("X-Github-Hook-Id", provider.getId())
                        .header("X-DevPlatform-Status-Context", statusContext)
                        .header("X-Github-Hook-Installation-Target-Type", "repository");

                if (provider.getOutboundSigningSecret() != null && !provider.getOutboundSigningSecret().isBlank()) {
                    String sig = signatureVerifier.sign(provider.getOutboundSigningSecret(), rawBody);
                    spec = spec.header("X-Outbound-Signature-256", sig);
                    //Add sha1 if you want ofc
                }

                String jsonBody = new String(rawBody, StandardCharsets.UTF_8);
                spec.body(jsonBody).retrieve().toBodilessEntity();
                LOGGER.info("Fan-out OK provider={} url={} sha={}", provider.getId(), provider.getInboundWebhookUrl(), parsed.resolveSha());
            } catch (Exception e) {
                LOGGER.warn("Fan-out FAILED provider={} url={} err={}", provider.getId(), provider.getInboundWebhookUrl(), e.getMessage());
            }
        }
    }

}
