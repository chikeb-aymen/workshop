import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentService(List<PaymentStrategy> strategyBeans) {
        this.strategies = PaymentStrategy.index(strategyBeans);
    }

    public void pay(String type, double amount) {
        PaymentStrategy.required(strategies, type).pay(amount);
    }
}
