package org.me.agentcore.controller;

import org.me.agentcore.agent.AgentRunLoopService;
import org.me.agentcore.agent.TradingAgent;
import org.me.agentcore.agent.AgentLifecycleService;
import org.me.agentcore.domain.AgentStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final TradingAgent tradingAgent;
    private final AgentLifecycleService agentLifecycleService;
    private final AgentRunLoopService agentRunLoopService;

    public AgentController(
            TradingAgent tradingAgent,
            AgentLifecycleService agentLifecycleService,
            AgentRunLoopService agentRunLoopService
    ) {
        this.tradingAgent = tradingAgent;
        this.agentLifecycleService = agentLifecycleService;
        this.agentRunLoopService = agentRunLoopService;
    }

    @GetMapping("/status")
    public ResponseEntity<AgentStatus> getStatus() {
        return ResponseEntity.ok(agentLifecycleService.getStatus());
    }

    @PostMapping("/start")
    public ResponseEntity<AgentStatus> start() {
        return ResponseEntity.ok(agentRunLoopService.start());
    }

    @PostMapping("/stop")
    public ResponseEntity<AgentStatus> stop() {
        return ResponseEntity.ok(agentRunLoopService.stop());
    }

    @PostMapping("/run-once/{ticker}")
    public ResponseEntity<String> runOnce(@PathVariable String ticker) {
        tradingAgent.runOnce(ticker);
        return ResponseEntity.ok("Trading agent run completed for ticker " + ticker + " at " + Instant.now());
    }
}
