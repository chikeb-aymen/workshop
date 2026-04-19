public class InventoryObserver implements OrderObserver {
    @Override
    public void onOrderPlaced(Order order) {
        System.out.println("[Inventory] Reducing stock for: " + order.getProduct()
                + " (qty: " + order.getQuantity() + ")");
    }
}
