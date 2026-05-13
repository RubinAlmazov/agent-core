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
    public void recordDecisionContext(DecisionContext context) {
        recordPortfolioSnapshot(context.portfolioState());
    }

    @Override
    public long recordTradeDecision(DecisionContext context, TradeDecision decision) {
        return decisionJournalRepository.save(context, decision);
    }

    @Override
    public long recordRiskCheck(long decisionId, RiskCheckResult result) {
        return riskCheckJournalRepository.save(decisionId, result);
    }

    @Override
    public long recordOrderResult(long decisionId, long riskCheckId, OrderRequest request, OrderResult result) {
        return orderJournalRepository.save(decisionId, riskCheckId, request, result);
    }

    @Override
    public long recordPortfolioSnapshot(PortfolioState portfolioState) {
        return portfolioSnapshotJournalRepository.save(portfolioState);
    }
}
