public class PaymentService {
    public void pay(String type, double amount) {
        if (type.equals("card")) {
            System.out.println("Paying " + amount + " with credit card");
        } else if (type.equals("paypal")) {
            System.out.println("Paying " + amount + " with PayPal");
        } else if (type.equals("crypto")) {
            System.out.println("Paying " + amount + " with crypto");
        }
    }
}
