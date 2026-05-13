package org.me.agentcore.service.impl;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.OrderRequest;
import org.me.agentcore.domain.OrderResult;
import org.me.agentcore.domain.PortfolioState;
import org.me.agentcore.domain.RiskCheckResult;
import org.me.agentcore.domain.TradeDecision;
import org.me.agentcore.repository.DecisionJournalRepository;
import org.me.agentcore.repository.OrderJournalRepository;
import org.me.agentcore.repository.PortfolioSnapshotJournalRepository;
import org.me.agentcore.repository.RiskCheckJournalRepository;
import org.me.agentcore.service.JournalService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class PostgresJournalService implements JournalService {

    private final DecisionJournalRepository decisionJournalRepository;
    private final RiskCheckJournalRepository riskCheckJournalRepository;
    private final OrderJournalRepository orderJournalRepository;
    private final PortfolioSnapshotJournalRepository portfolioSnapshotJournalRepository;

    public PostgresJournalService(
            DecisionJournalRepository decisionJournalRepository,
            RiskCheckJournalRepository riskCheckJournalRepository,
            OrderJournalRepository orderJournalRepository,
            PortfolioSnapshotJournalRepository portfolioSnapshotJournalRepository
    ) {
        this.decisionJournalRepository = decisionJournalRepository;
        this.riskCheckJournalRepository = riskCheckJournalRepository;
        this.orderJournalRepository = orderJournalRepository;
        this.portfolioSnapshotJournalRepository = portfolioSnapshotJournalRepository;
    }

    @Override
    public void recordDecisionContext(Long agentRunId, DecisionContext context) {
        recordPortfolioSnapshot(agentRunId, context.portfolioState());
    }

    @Override
    public long recordTradeDecision(Long agentRunId, DecisionContext context, TradeDecision decision) {
        return decisionJournalRepository.save(agentRunId, context, decision);
    }

    @Override
    public long recordRiskCheck(Long agentRunId, long decisionId, RiskCheckResult result) {
        return riskCheckJournalRepository.save(decisionId, result);
    }

    @Override
    public long recordOrderResult(
            Long agentRunId,
            long decisionId,
            long riskCheckId,
            OrderRequest request,
            OrderResult result
    ) {
        return orderJournalRepository.save(agentRunId, decisionId, riskCheckId, request, result);
    }

    @Override
    public long recordPortfolioSnapshot(Long agentRunId, PortfolioState portfolioState) {
        return portfolioSnapshotJournalRepository.save(agentRunId, portfolioState);
    }
}
