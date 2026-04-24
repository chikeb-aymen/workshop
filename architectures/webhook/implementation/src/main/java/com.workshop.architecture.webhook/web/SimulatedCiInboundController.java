package com.workshop.architecture.webhook.web;

import com.workshop.architecture.webhook.simulation.CiSimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sim-ci")
public class SimulatedCiInboundController {

    private final CiSimulationService simulationService;

    public SimulatedCiInboundController(CiSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/{slug}/pipeline")
    public ResponseEntity<String> inbound(
            @PathVariable String slug,
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-DevPlatform-Status-Context", required = false) String statusContext) {

        simulationService.runPipeline(slug, rawBody, statusContext);
        return ResponseEntity.accepted().body("build scheduled");
    }
}
