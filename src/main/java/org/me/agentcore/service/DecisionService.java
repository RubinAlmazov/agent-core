package org.me.agentcore.service;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.DecisionResult;

public interface DecisionService {

    DecisionResult decide(DecisionContext context);
}
