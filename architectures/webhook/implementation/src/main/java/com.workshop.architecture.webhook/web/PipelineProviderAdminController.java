package com.workshop.architecture.webhook.web;

import com.workshop.architecture.webhook.ci.PipelineProviderRegistry;
import com.workshop.architecture.webhook.model.PipelineProviderConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/pipeline-providers")
public class PipelineProviderAdminController {

    private final PipelineProviderRegistry registry;

    public PipelineProviderAdminController(PipelineProviderRegistry registry) {
        this.registry = registry;
    }

    @GetMapping
    public List<PipelineProviderConfig> list() {
        return registry.snapshot();
    }

    @PutMapping
    public ResponseEntity<Void> register(@RequestBody PipelineProviderConfig config) {
        if (config.getId() == null || config.getInboundWebhookUrl() == null) {
            return ResponseEntity.badRequest().build();
        }
        registry.register(config);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable String id) {
        registry.remove(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
