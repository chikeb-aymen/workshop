import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        WebhookRegistry registry = new WebhookRegistry();
        WebhookSigner signer = new WebhookSigner();
        WebhookDispatcher dispatcher = new WebhookDispatcher(registry, signer);

        registry.register(new WebhookSubscriber(
                "sub_shopfront", "ShopFront", "https://shopfront.io/webhooks",
                "payment.succeeded", "secret_sf_abc123"));

        registry.register(new WebhookSubscriber(
                "sub_fintrack", "FinanceTracker", "https://fintrack.io/webhooks",
                "payment.succeeded", "secret_ft_xyz789"));

        registry.register(new WebhookSubscriber(
                "sub_fraud", "FraudShield", "https://fraudshield.io/webhooks",
                "payment.failed", "secret_fs_def456"));

        LOGGER.info("Scenario start — payment.succeeded");

        WebhookEvent paymentSucceeded = new WebhookEvent(
                "payment.succeeded",
                Map.of("payment_id", "pay_abc", "order_id", "ord_001",
                        "amount", "9900", "currency", "USD")
        );
        dispatcher.dispatch(paymentSucceeded);

        LOGGER.info("Scenario start — payment.failed");

        WebhookEvent paymentFailed = new WebhookEvent(
                "payment.failed",
                Map.of("payment_id", "pay_xyz", "order_id", "ord_002",
                        "failure_reason", "INSUFFICIENT_FUNDS")
        );
        dispatcher.dispatch(paymentFailed);

        LOGGER.info("Consumer path — routing inbound event to first matching handler");

        List<WebhookHandler> handlers = List.of(
                new PaymentSucceededHandler(),
                new PaymentFailedHandler(),
                new RefundIssuedHandler()
        );

        WebhookEvent inbound = new WebhookEvent(
                "payment.succeeded",
                Map.of("payment_id", "pay_abc", "order_id", "ord_001",
                        "amount", "9900", "currency", "USD")
        );

        handlers.stream()
                .filter(h -> h.supports(inbound.getEventType()))
                .findFirst()
                .ifPresentOrElse(
                        h -> h.handle(inbound),
                        () -> LOGGER.warn("No handler matched eventType={} eventId={}",
                                inbound.getEventType(), inbound.getEventId())
                );

        LOGGER.info("Demo finished");
    }
}
