public class App {

    public static void main(String[] args) {

        PaymentStrategy strategy = new CreditCardPayment();

        PaymentService service = new PaymentService(strategy);

        service.pay(100); //Paid 100 using credit card

    }

}
