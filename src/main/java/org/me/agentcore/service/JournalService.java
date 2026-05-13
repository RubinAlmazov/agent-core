package org.me.agentcore.service;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.OrderRequest;
import org.me.agentcore.domain.OrderResult;
import org.me.agentcore.domain.PortfolioState;
import org.me.agentcore.domain.RiskCheckResult;
import org.me.agentcore.domain.TradeDecision;

public interface JournalService {

    void recordDecisionContext(Long agentRunId, DecisionContext context);

    long recordTradeDecision(Long agentRunId, DecisionContext context, TradeDecision decision);

    long recordRiskCheck(Long agentRunId, long decisionId, RiskCheckResult result);

    long recordOrderResult(Long agentRunId, long decisionId, long riskCheckId, OrderRequest request, OrderResult result);

    long recordPortfolioSnapshot(Long agentRunId, PortfolioState portfolioState);
}
