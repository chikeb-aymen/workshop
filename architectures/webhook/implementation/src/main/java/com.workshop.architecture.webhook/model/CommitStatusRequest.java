package com.workshop.architecture.webhook.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitStatusRequest {

    private String state;

    private String description;

    private String context;

    @JsonProperty("target_url")
    private String targetUrl;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
