package com.workshop.architecture.webhook.bootstrap;

import com.workshop.architecture.webhook.ci.PipelineProviderRegistry;
import com.workshop.architecture.webhook.config.AppProperties;
import com.workshop.architecture.webhook.model.PipelineProviderConfig;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoPipelineBootstrap {

    @Bean
    ApplicationRunner registerDemoProviders(AppProperties props, PipelineProviderRegistry registry) {
        return args -> {
            String base = props.getPublicBaseUrl().replaceAll("/$", "");

            PipelineProviderConfig circle = new PipelineProviderConfig();
            circle.setId("circle-demo");
            circle.setLabel("continuous-integration/circle-ci");
            circle.setInboundWebhookUrl(base + "/sim-ci/circle/pipeline");
            registry.register(circle);

            PipelineProviderConfig custom = new PipelineProviderConfig();
            custom.setId("custom-demo");
            custom.setLabel("continuous-integration/custom-pipeline");
            custom.setInboundWebhookUrl(base + "/sim-ci/custom/pipeline");
            registry.register(custom);
        };
    }
}
