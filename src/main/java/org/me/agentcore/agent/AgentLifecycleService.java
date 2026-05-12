package org.me.agentcore.agent;

import org.me.agentcore.domain.AgentStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class AgentLifecycleService {

    private final AtomicReference<AgentStatus> status = new AtomicReference<>(AgentStatus.STOPPED);

    public AgentStatus getStatus() {
        return status.get();
    }

    public AgentStatus start() {
        status.set(AgentStatus.STARTING);
        status.set(AgentStatus.RUNNING);
        return status.get();
    }

    public AgentStatus stop() {
        status.set(AgentStatus.STOPPED);
        return status.get();
    }

    public AgentStatus markFailed() {
        status.set(AgentStatus.FAILED);
        return status.get();
    }
}
