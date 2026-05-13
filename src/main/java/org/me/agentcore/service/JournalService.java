package org.me.agentcore.service;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.OrderRequest;
import org.me.agentcore.domain.OrderResult;
import org.me.agentcore.domain.PortfolioState;
import org.me.agentcore.domain.RiskCheckResult;
import org.me.agentcore.domain.TradeDecision;

public interface JournalService {

    void recordDecisionContext(DecisionContext context);

    long recordTradeDecision(DecisionContext context, TradeDecision decision);

    long recordRiskCheck(long decisionId, RiskCheckResult result);

    long recordOrderResult(long decisionId, long riskCheckId, OrderRequest request, OrderResult result);

    long recordPortfolioSnapshot(PortfolioState portfolioState);
}
