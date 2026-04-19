public interface OrderSubject {
    void subscribe(OrderObserver observer);

    void unsubscribe(OrderObserver observer);

    void notifyObservers(Order order);
}
