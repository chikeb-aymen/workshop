package com.workshop.architecture.webhook.web;

import com.workshop.architecture.webhook.model.CommitStatusRequest;
import com.workshop.architecture.webhook.status.CommitStatusLog;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fake-github/api/repos")
public class FakeGitHubStatusesController {

    private final CommitStatusLog commitStatusLog;

    public FakeGitHubStatusesController(CommitStatusLog commitStatusLog) {
        this.commitStatusLog = commitStatusLog;
    }

    @PostMapping("/{owner}/{repo}/statuses/{sha}")
    public void create(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String sha,
            @RequestBody CommitStatusRequest body) {
        commitStatusLog.add(owner, repo, sha, body.getState(), body.getContext(), body.getDescription());
        //And here we can add commit status by repo and also by jobs
    }
}
