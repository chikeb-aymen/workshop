public class AnalyticsObserver implements OrderObserver {
    @Override
    public void onOrderPlaced(Order order) {
        System.out.println("[Analytics] Logging order event: " + order.getId());
    }
}
