package org.me.agentcore.service;

import org.me.agentcore.domain.PortfolioState;
import org.me.agentcore.domain.RiskCheckResult;
import org.me.agentcore.domain.TradeDecision;

public interface RiskManager {

    RiskCheckResult check(TradeDecision decision, PortfolioState portfolioState);
}
