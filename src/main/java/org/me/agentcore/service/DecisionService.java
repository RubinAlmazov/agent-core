package org.me.agentcore.service;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.TradeDecision;

public interface DecisionService {

    TradeDecision decide(DecisionContext context);
}
