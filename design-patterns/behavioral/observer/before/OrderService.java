public class OrderService {
    private EmailService emailService = new EmailService();
    private InventoryService inventoryService = new InventoryService();
    private WarehouseService warehouseService = new WarehouseService();
    private AnalyticsService analyticsService = new AnalyticsService();

    public void placeOrder(Order order) {
        // Core logic
        System.out.println("Order placed: " + order.getId());
        // Directly calling everything — tightly coupled
        emailService.sendConfirmation(order);
        inventoryService.reduceStock(order);
        warehouseService.notifyShipment(order);
        analyticsService.logOrder(order);
        // Next week: add SMS → must open this file again
        // smsService.sendSms(order);
        // loyaltyService.addPoints(order);  ← keeps growing forever
    }
}
