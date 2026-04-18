public class CreditCardPayment implements PaymentStrategy {

    public void pay(double amount) {
        System.out.println("Paid " + amount + " using credit card");
    }

}
