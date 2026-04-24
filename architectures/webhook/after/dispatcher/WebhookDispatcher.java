import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WebhookDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookDispatcher.class);

    private static final int BODY_LOG_MAX = 512;

    private final WebhookRegistry registry;
    private final WebhookSigner signer;

    public WebhookDispatcher(WebhookRegistry registry, WebhookSigner signer) {
        this.registry = registry;
        this.signer = signer;
    }

    /**
     * Dispatch an event to all registered subscribers.
     * In production: enqueue to a job queue and let workers do the HTTP POST asynchronously.
     */
    public void dispatch(WebhookEvent event) {
        List<WebhookSubscriber> subscribers = registry.getSubscribers(event.getEventType());
        if (subscribers.isEmpty()) {
            LOGGER.warn("No subscribers registered for eventType={} eventId={}", event.getEventType(), event.getEventId());
            return;
        }

        LOGGER.info("Dispatching webhook eventType={} eventId={} subscriberCount={}",
                event.getEventType(), event.getEventId(), subscribers.size());

        String rawBody = event.toJson();

        for (WebhookSubscriber subscriber : subscribers) {
            String signature = signer.sign(subscriber.getSecretKey(), rawBody);
            deliver(subscriber, rawBody, signature, event);
        }
    }

    private void deliver(WebhookSubscriber subscriber, String rawBody,
                         String signature, WebhookEvent event) {
        LOGGER.info("Outbound delivery eventId={} subscriberId={} subscriberName={} url={}",
                event.getEventId(), subscriber.getId(), subscriber.getName(), subscriber.getUrl());

        LOGGER.debug("Delivery HTTP details eventId={} signaturePrefix={} bodySnippet={}",
                event.getEventId(),
                signaturePreview(signature),
                truncateForLog(rawBody));

        boolean success = simulateHttpPost(subscriber.getUrl(), rawBody, signature);

        if (success) {
            LOGGER.info("Delivery succeeded eventId={} subscriberName={}", event.getEventId(), subscriber.getName());
        } else {
            LOGGER.warn("Delivery failed eventId={} subscriberName={} — would schedule retry with backoff",
                    event.getEventId(), subscriber.getName());
        }
    }

    private static String signaturePreview(String signature) {
        if (signature == null || signature.length() <= 16) {
            return "***";
        }
        return signature.substring(0, 16) + "…";
    }

    private static String truncateForLog(String rawBody) {
        if (rawBody == null) {
            return "";
        }
        if (rawBody.length() <= BODY_LOG_MAX) {
            return rawBody;
        }
        return rawBody.substring(0, BODY_LOG_MAX) + "…(truncated)";
    }

    private boolean simulateHttpPost(String url, String body, String signature) {
        return true;
    }
}
