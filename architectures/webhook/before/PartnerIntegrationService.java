import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartnerIntegrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartnerIntegrationService.class);

    private final PaymentGatewayClient gatewayClient = new PaymentGatewayClient();

    private final List<String> trackedPaymentIds = new CopyOnWriteArrayList<>();

    private volatile boolean running = true;

    public void trackPayment(String paymentId) {
        trackedPaymentIds.add(paymentId);
        LOGGER.info("Tracking payment {}", paymentId);
    }

    public void startPolling() {
        LOGGER.info("[PartnerIntegration] Starting polling loop (every 5s)...");

        while (running) {
            for (String paymentId : trackedPaymentIds) {
                String status = gatewayClient.getPaymentStatus(paymentId);

                if ("SUCCEEDED".equals(status)) {
                    notifyShopFront(paymentId);
                    notifyFinanceTracker(paymentId);
                    notifyFraudShield(paymentId, status);
                    trackedPaymentIds.remove(paymentId);

                } else if ("FAILED".equals(status)) {
                    notifyFinanceTracker(paymentId);
                    notifyFraudShield(paymentId, status);
                    trackedPaymentIds.remove(paymentId);
                }
            }

            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Polling thread interrupted", e);
            }
        }
    }

    public void stop() {
        running = false;
        LOGGER.info("Stopping polling loop");
    }

    private void notifyShopFront(String paymentId) {
        LOGGER.info("[ShopFront] Marking order paid for payment {}", paymentId);
    }

    private void notifyFinanceTracker(String paymentId) {
        LOGGER.info("[FinanceTracker] Logging revenue event for {}", paymentId);
    }

    private void notifyFraudShield(String paymentId, String status) {
        LOGGER.info("[FraudShield] Scoring payment {} with status {}", paymentId, status);
    }
    // Next partner → open this class again.
    // And the class after that → open this class again.
}
