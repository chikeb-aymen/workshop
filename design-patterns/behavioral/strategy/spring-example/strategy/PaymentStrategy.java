import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface PaymentStrategy {

    String getStrategyKey();

    void pay(double amount);

    static Map<String, PaymentStrategy> index(Collection<PaymentStrategy> strategies) {
        return strategies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        s -> s.getStrategyKey().toLowerCase(Locale.ROOT),
                        Function.identity(),
                        (a, b) -> {
                            throw new IllegalStateException(
                                    "Duplicate payment strategy key: " + a.getStrategyKey());
                        }));
    }

    static PaymentStrategy required(Map<String, PaymentStrategy> strategies, String type) {
        PaymentStrategy strategy = strategies.get(type.toLowerCase(Locale.ROOT));
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown payment type: " + type);
        }
        return strategy;
    }
}
