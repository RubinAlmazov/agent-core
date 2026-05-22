package org.me.agentcore.service;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.DecisionResult;
import org.me.agentcore.domain.OrderRequest;
import org.me.agentcore.domain.OrderResult;
import org.me.agentcore.domain.PortfolioState;
import org.me.agentcore.domain.RiskCheckResult;

public interface JournalService {

    void recordDecisionContext(Long agentRunId, DecisionContext context);

    long recordDecisionResult(Long agentRunId, DecisionContext context, DecisionResult decisionResult);

    long recordRiskCheck(Long agentRunId, long decisionId, RiskCheckResult result);

    long recordOrderResult(Long agentRunId, long decisionId, long riskCheckId, OrderRequest request, OrderResult result);

    long recordPortfolioSnapshot(Long agentRunId, PortfolioState portfolioState);
}
