import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefundIssuedHandler implements WebhookHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefundIssuedHandler.class);

    @Override
    public boolean supports(String eventType) {
        return "refund.issued".equals(eventType);
    }

    @Override
    public void handle(WebhookEvent event) {
        String refundId = (String) event.getData().get("refund_id");
        String amount = (String) event.getData().get("amount");
        LOGGER.info("Refund issued eventId={} eventType={} refundId={} amountCents={}", event.getEventId(), event.getEventType(), refundId, amount);
    }
}
