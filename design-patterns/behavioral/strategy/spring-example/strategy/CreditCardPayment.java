import org.springframework.stereotype.Component;

@Component
public class CreditCardPayment implements PaymentStrategy {

    @Override
    public String getStrategyKey() {
        return "card";
    }

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using credit card");
    }
}
