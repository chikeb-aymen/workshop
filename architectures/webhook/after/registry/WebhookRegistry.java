import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebhookRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookRegistry.class);

    private final Map<String, List<WebhookSubscriber>> subscriptions = new ConcurrentHashMap<>();

    public void register(WebhookSubscriber subscriber) {
        subscriptions
                .computeIfAbsent(subscriber.getEventType(), k -> new CopyOnWriteArrayList<>())
                .add(subscriber);
        LOGGER.info("Subscriber registered subscriberId={} name={} eventType={} url={}",
                subscriber.getId(), subscriber.getName(), subscriber.getEventType(), subscriber.getUrl());
    }

    public void unregister(String subscriberId, String eventType) {
        List<WebhookSubscriber> list = subscriptions.getOrDefault(eventType, List.of());
        boolean removed = list.removeIf(s -> s.getId().equals(subscriberId));
        if (removed) {
            LOGGER.info("Subscriber removed subscriberId={} eventType={}", subscriberId, eventType);
        } else {
            LOGGER.debug("Unregister noop — no subscriber subscriberId={} eventType={}", subscriberId, eventType);
        }
    }

    public List<WebhookSubscriber> getSubscribers(String eventType) {
        return subscriptions.getOrDefault(eventType, List.of());
    }
}
