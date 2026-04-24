import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentFailedHandler implements WebhookHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentFailedHandler.class);

    @Override
    public boolean supports(String eventType) {
        return "payment.failed".equals(eventType);
    }

    @Override
    public void handle(WebhookEvent event) {
        String paymentId = (String) event.getData().get("payment_id");
        String reason = (String) event.getData().get("failure_reason");
        LOGGER.warn("Payment failed eventId={} eventType={} paymentId={} reason={}",
                event.getEventId(), event.getEventType(), paymentId, reason);
    }
}
