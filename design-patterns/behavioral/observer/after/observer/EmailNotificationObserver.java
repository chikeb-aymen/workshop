public class EmailNotificationObserver implements OrderObserver {
    @Override
    public void onOrderPlaced(Order order) {
        System.out.println("[Email] Sending confirmation for order: " + order.getId());
    }
}
