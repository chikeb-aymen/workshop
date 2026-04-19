public class WarehouseObserver implements OrderObserver {
    @Override
    public void onOrderPlaced(Order order) {
        System.out.println("[Warehouse] Preparing shipment for order: " + order.getId());
    }
}