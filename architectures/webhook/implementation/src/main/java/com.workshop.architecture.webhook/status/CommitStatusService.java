package com.workshop.architecture.webhook.status;

import com.workshop.architecture.webhook.model.CommitStatusRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CommitStatusService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommitStatusService.class);

    private final RestClient restClient;
    private final String fakeGithubBaseUrl;

    public CommitStatusService(
            RestClient restClient,
            @Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.restClient = restClient;
        this.fakeGithubBaseUrl = publicBaseUrl.replaceAll("/$", "") + "/fake-github/api/repos";
    }

    public void postStatus(String owner, String repo, String sha, CommitStatusRequest body) {
        String path = "/%s/%s/statuses/%s".formatted(owner, repo, sha);
        try {
            restClient.post()
                    .uri(fakeGithubBaseUrl + path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            LOGGER.info("Commit status posted context={} state={} sha={}", body.getContext(), body.getState(), sha);
        } catch (Exception e) {
            LOGGER.warn("Commit status POST failed: {}", e.getMessage());
        }
    }
}
