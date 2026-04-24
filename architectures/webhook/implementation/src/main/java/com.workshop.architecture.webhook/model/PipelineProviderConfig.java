package com.workshop.architecture.webhook.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PipelineProviderConfig {

    private String id;

    private String label;

    @JsonProperty("inboundWebhookUrl")
    private String inboundWebhookUrl;

    @JsonProperty("outboundSigningSecret")
    private String outboundSigningSecret;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getInboundWebhookUrl() {
        return inboundWebhookUrl;
    }

    public void setInboundWebhookUrl(String inboundWebhookUrl) {
        this.inboundWebhookUrl = inboundWebhookUrl;
    }

    public String getOutboundSigningSecret() {
        return outboundSigningSecret;
    }

    public void setOutboundSigningSecret(String outboundSigningSecret) {
        this.outboundSigningSecret = outboundSigningSecret;
    }
}
