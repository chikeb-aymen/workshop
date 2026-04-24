package com.workshop.architecture.webhook.status;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class CommitStatusLog {

    private final List<Entry> entries = new CopyOnWriteArrayList<>();

    public void add(String owner, String repo, String sha, String state, String context, String description) {
        entries.add(0, new Entry(Instant.now(), owner, repo, sha, state, context, description));
    }

    public List<Entry> recent() {
        return List.copyOf(entries);
    }

    public record Entry(
            Instant at,
            String owner,
            String repo,
            String sha,
            String state,
            String context,
            String description
    ) {
    }
}
