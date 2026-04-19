public class OrderService implements OrderSubject {
    private final List<OrderObserver> observers = new ArrayList<>();

    @Override
    public void subscribe(OrderObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(OrderObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Order order) {
        for (OrderObserver observer : observers) {
            observer.onOrderPlaced(order);
        }
    }

    public void placeOrder(Order order) {
        System.out.println("[OrderService] Order placed: " + order.getId());
        notifyObservers(order);  // That's all. No knowledge of who reacts.
    }
}
