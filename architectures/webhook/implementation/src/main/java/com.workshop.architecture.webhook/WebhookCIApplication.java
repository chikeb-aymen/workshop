package com.workshop.architecture.webhook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WebhookCIApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebhookCiApplication.class, args);
    }
}
