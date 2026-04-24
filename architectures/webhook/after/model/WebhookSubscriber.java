public class WebhookSubscriber {

    private final String id;
    private final String name;
    private final String url;
    private final String eventType; // Sometimes can be list of events
    private final String secretKey; // shared HMAC secret - never LOGGER this

    public WebhookSubscriber(String id, String name, String url,
                             String eventType, String secretKey) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.eventType = eventType;
        this.secretKey = secretKey;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSecretKey() {
        return secretKey;
    }

    @Override
    public String toString() {
        return name + "[" + url + "]";
    }
}
