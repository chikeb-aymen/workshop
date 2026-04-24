import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentSucceededHandler implements WebhookHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentSucceededHandler.class);

    @Override
    public boolean supports(String eventType) {
        return "payment.succeeded".equals(eventType);
    }

    @Override
    public void handle(WebhookEvent event) {
        String paymentId = (String) event.getData().get("payment_id");
        String orderId = (String) event.getData().get("order_id");
        LOGGER.info("Order marked paid eventId={} eventType={} orderId={} paymentId={}",
                event.getEventId(), event.getEventType(), orderId, paymentId);
    }
}
