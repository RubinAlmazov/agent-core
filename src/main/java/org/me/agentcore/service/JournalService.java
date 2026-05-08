package org.me.agentcore.service;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.OrderResult;
import org.me.agentcore.domain.RiskCheckResult;
import org.me.agentcore.domain.TradeDecision;

public interface JournalService {

    void recordDecisionContext(DecisionContext context);

    void recordTradeDecision(TradeDecision decision);

    void recordRiskCheck(RiskCheckResult result);

    void recordOrderResult(OrderResult result);
}
