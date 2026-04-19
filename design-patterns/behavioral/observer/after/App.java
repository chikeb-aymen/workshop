public class App {
    public static void main(String[] args) {
        OrderService orderService = new OrderService();

        // Register observers — can add or remove at any time
        orderService.subscribe(new EmailNotificationObserver());
        orderService.subscribe(new InventoryObserver());
        orderService.subscribe(new WarehouseObserver());
        orderService.subscribe(new AnalyticsObserver());

        // Place an order — all observers react automatically
        Order order = new Order("ORD-001", "Laptop", 1);
        orderService.placeOrder(order);

        System.out.println("---");

        // Adding a new observer (SMS) — OrderService is NEVER touched
        orderService.subscribe(observer ->
                System.out.println("[SMS] Sending SMS for order: " + observer.getId())
        );

        Order order2 = new Order("ORD-002", "Phone", 2);
        orderService.placeOrder(order2);
    }

    // Expected Output
    //[OrderService] Order placed: ORD-001
    //        [Email] Sending confirmation for order: ORD-001
    //        [Inventory] Reducing stock for: Laptop (qty: 1)
    //[Warehouse] Preparing shipment for order: ORD-001
    //        [Analytics] Logging order event: ORD-001
    //        ---
    //        [OrderService] Order placed: ORD-002
    //        [Email] Sending confirmation for order: ORD-002
    //        [Inventory] Reducing stock for: Phone (qty: 2)
    //[Warehouse] Preparing shipment for order: ORD-002
    //        [Analytics] Logging order event: ORD-002
    //        [SMS] Sending SMS for order: ORD-002
}
