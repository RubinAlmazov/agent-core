package org.me.agentcore.agent;

import org.me.agentcore.config.TradingProperties;
import org.me.agentcore.domain.AgentStatus;
import org.me.agentcore.repository.AgentRunRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class AgentRunLoopService {

    private final TradingAgent tradingAgent;
    private final AgentLifecycleService agentLifecycleService;
    private final TradingProperties tradingProperties;
    private final AgentRunRepository agentRunRepository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> runningTask;
    private Long currentAgentRunId;

    public AgentRunLoopService(
            TradingAgent tradingAgent,
            AgentLifecycleService agentLifecycleService,
            TradingProperties tradingProperties,
            AgentRunRepository agentRunRepository
    ) {
        this.tradingAgent = tradingAgent;
        this.agentLifecycleService = agentLifecycleService;
        this.tradingProperties = tradingProperties;
        this.agentRunRepository = agentRunRepository;
    }

    public synchronized AgentStatus start() {
        if (runningTask != null && !runningTask.isDone()) {
            return agentLifecycleService.getStatus();
        }

        agentLifecycleService.start();
        currentAgentRunId = agentRunRepository.startRun(
                agentLifecycleService.getStatus(),
                tradingProperties.getMode().name()
        );
        runningTask = executorService.submit(this::runLoop);
        return agentLifecycleService.getStatus();
    }

    public synchronized AgentStatus stop() {
        if (runningTask != null) {
            runningTask.cancel(true);
            runningTask = null;
        }

        AgentStatus status = agentLifecycleService.stop();
        stopCurrentAgentRun(status);
        return status;
    }

    @PreDestroy
    public synchronized void shutdown() {
        if (runningTask != null) {
            runningTask.cancel(true);
            runningTask = null;
        }

        stopCurrentAgentRun(AgentStatus.STOPPED);
        executorService.shutdownNow();
    }

    private void runLoop() {
        try {
            while (agentLifecycleService.getStatus() == AgentStatus.RUNNING) {
                runOneIteration();
                sleep(tradingProperties.getCycleInterval());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (Exception exception) {
            agentLifecycleService.markFailed();
            failCurrentAgentRun(exception);
        }
    }

    private void stopCurrentAgentRun(AgentStatus status) {
        if (currentAgentRunId == null) {
            return;
        }

        agentRunRepository.stopRun(currentAgentRunId, status);
        currentAgentRunId = null;
    }

    private void failCurrentAgentRun(Exception exception) {
        if (currentAgentRunId == null) {
            return;
        }

        agentRunRepository.failRun(currentAgentRunId, exception.getMessage());
        currentAgentRunId = null;
    }

    private void runOneIteration() {
        for (String ticker : tradingProperties.getTickers()) {
            if (agentLifecycleService.getStatus() != AgentStatus.RUNNING) {
                return;
            }

            tradingAgent.runOnce(ticker, currentAgentRunId);
        }
    }

    private void sleep(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }
}
