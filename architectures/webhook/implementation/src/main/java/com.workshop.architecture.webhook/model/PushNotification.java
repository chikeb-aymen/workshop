package com.workshop.architecture.webhook.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PushNotification {

    private String ref;
    @JsonProperty("head_commit")
    private HeadCommit headCommit;
    private Repository repository;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public HeadCommit getHeadCommit() {
        return headCommit;
    }

    public void setHeadCommit(HeadCommit headCommit) {
        this.headCommit = headCommit;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public String resolveSha() {
        return headCommit != null ? headCommit.getId() : null;
    }

    public String resolveFullName() {
        return repository != null ? repository.getFullName() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HeadCommit {

        private String id;
        private String message;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {

        @JsonProperty("full_name")
        private String fullName;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
}