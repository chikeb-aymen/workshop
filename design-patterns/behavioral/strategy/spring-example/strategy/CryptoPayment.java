import org.springframework.stereotype.Component;

@Component
public class CryptoPayment implements PaymentStrategy {

    @Override
    public String getStrategyKey() {
        return "crypto";
    }

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using Crypto");
    }
}
