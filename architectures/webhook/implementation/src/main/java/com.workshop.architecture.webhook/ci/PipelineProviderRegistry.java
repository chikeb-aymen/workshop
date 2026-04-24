package com.workshop.architecture.webhook.ci;

import com.workshop.architecture.webhook.model.PipelineProviderConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class PipelineProviderRegistry {

    private final List<PipelineProviderConfig> providers = new CopyOnWriteArrayList<>();

    public List<PipelineProviderConfig> snapshot() {
        return new ArrayList<>(providers);
    }

    public void register(PipelineProviderConfig config) {
        providers.removeIf(p -> p.getId().equals(config.getId()));
        providers.add(config);
    }

    public boolean remove(String id) {
        return providers.removeIf(p -> p.getId().equals(id));
    }

    public Optional<PipelineProviderConfig> find(String id) {
        return providers.stream().filter(p -> p.getId().equals(id)).findFirst();
    }
}
