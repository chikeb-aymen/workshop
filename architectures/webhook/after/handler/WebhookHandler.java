/**
 * Consumer-side contract.
 * Each implementation handles one event type received at the consumer's webhook endpoint.
 */
public interface WebhookHandler {
    boolean supports(String eventType);

    void handle(WebhookEvent event);
}
