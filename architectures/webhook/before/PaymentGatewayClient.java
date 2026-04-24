import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentGatewayClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentGatewayClient.class);

    public String getPaymentStatus(String paymentId) {
        // Simulates calling GET /payments/{id}/status
        LOGGER.info("[GatewayClient] Polling status for payment: {}", paymentId);
        // In reality: HttpClient.newHttpClient().send(request, ...) and parse JSON
        return "SUCCEEDED";
    }
}
