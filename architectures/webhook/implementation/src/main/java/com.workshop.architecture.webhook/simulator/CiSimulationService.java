package com.workshop.architecture.webhook.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workshop.architecture.webhook.model.CommitStatusRequest;
import com.workshop.architecture.webhook.model.PushNotification;
import com.workshop.architecture.webhook.status.CommitStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CiSimulationService {

    private static final Logger log = LoggerFactory.getLogger(CiSimulationService.class);

    private final ObjectMapper objectMapper;
    private final CommitStatusService commitStatusService;

    public CiSimulationService(ObjectMapper objectMapper, CommitStatusService commitStatusService) {
        this.objectMapper = objectMapper;
        this.commitStatusService = commitStatusService;
    }

    @Async
    public void runPipeline(String slug, byte[] rawBody, String statusContextHeader) {
        try {
            PushNotification push = objectMapper.readValue(rawBody, PushNotification.class);
            String full = push.resolveFullName();
            String sha = push.resolveSha();
            if (full == null || sha == null || !full.contains("/")) {
                log.warn("Simulated CI: missing repository or sha in payload");
                return;
            }
            String[] parts = full.split("/", 2);
            String owner = parts[0];
            String repo = parts[1];

            String context = statusContextHeader != null && !statusContextHeader.isBlank()
                    ? statusContextHeader
                    : "ci/" + slug;

            post(owner, repo, sha, context, "pending", "Build queued", null);
            long workMs = "circle".equalsIgnoreCase(slug) ? 1200L : 800L;
            Thread.sleep(workMs);

            boolean success = !"custom".equalsIgnoreCase(slug);
            String state = success ? "success" : "failure";
            String description = success ? "Build finished OK" : "Build failed (simulated)";
            String target = "http://localhost/builds/" + sha + "/" + slug;

            post(owner, repo, sha, context, state, description, target);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Simulated CI pipeline error: {}", e.getMessage());
        }
    }

    private void post(String owner, String repo, String sha, String context,
                      String state, String description, String targetUrl) {
        CommitStatusRequest body = new CommitStatusRequest();
        body.setState(state);
        body.setDescription(description);
        body.setContext(context);
        body.setTargetUrl(targetUrl);
        commitStatusService.postStatus(owner, repo, sha, body);
    }
}
