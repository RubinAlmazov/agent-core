package org.me.agentcore.agent;

import org.me.agentcore.config.TradingProperties;
import org.me.agentcore.domain.AgentStatus;
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
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> runningTask;

    public AgentRunLoopService(
            TradingAgent tradingAgent,
            AgentLifecycleService agentLifecycleService,
            TradingProperties tradingProperties
    ) {
        this.tradingAgent = tradingAgent;
        this.agentLifecycleService = agentLifecycleService;
        this.tradingProperties = tradingProperties;
    }

    public synchronized AgentStatus start() {
        if (runningTask != null && !runningTask.isDone()) {
            return agentLifecycleService.getStatus();
        }

        agentLifecycleService.start();
        runningTask = executorService.submit(this::runLoop);
        return agentLifecycleService.getStatus();
    }

    public synchronized AgentStatus stop() {
        if (runningTask != null) {
            runningTask.cancel(true);
            runningTask = null;
        }

        return agentLifecycleService.stop();
    }

    @PreDestroy
    public synchronized void shutdown() {
        if (runningTask != null) {
            runningTask.cancel(true);
            runningTask = null;
        }

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
        }
    }

    private void runOneIteration() {
        for (String ticker : tradingProperties.getTickers()) {
            if (agentLifecycleService.getStatus() != AgentStatus.RUNNING) {
                return;
            }

            tradingAgent.runOnce(ticker);
        }
    }

    private void sleep(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }
}
