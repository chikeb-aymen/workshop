import org.springframework.stereotype.Component;

@Component
public class PaypalPayment implements PaymentStrategy {

    @Override
    public String getStrategyKey() {
        return "paypal";
    }

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using PayPal");
    }
}
