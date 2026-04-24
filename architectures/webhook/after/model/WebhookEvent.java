import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class WebhookEvent {

    private final String eventId;
    private final String eventType;
    private final String apiVersion;
    private final Instant createdAt;
    private final Map<String, Object> data;

    public WebhookEvent(String eventType, Map<String, Object> data) {
        this.eventId = "evt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        this.eventType = eventType;
        this.apiVersion = "2024-01-01";
        this.createdAt = Instant.now();
        this.data = Map.copyOf(data);
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String toJson() {
        // Minimal JSON serialization - replace with Jackson in real code
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"event_id\":\"").append(eventId).append("\",");
        sb.append("\"event_type\":\"").append(eventType).append("\",");
        sb.append("\"api_version\":\"").append(apiVersion).append("\",");
        sb.append("\"created_at\":\"").append(createdAt).append("\",");
        sb.append("\"data\":{");
        data.forEach((k, v) -> sb.append("\"").append(k).append("\":\"").append(v).append("\","));

        if (!data.isEmpty()) sb.deleteCharAt(sb.length() - 1);
        sb.append("}}");

        return sb.toString();
    }

    @Override
    public String toString() {
        return "WebhookEvent[" + eventType + "/" + eventId + "]";
    }
}
