package org.me.agentcore.service.impl;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.OrderResult;
import org.me.agentcore.domain.RiskCheckResult;
import org.me.agentcore.domain.TradeDecision;
import org.me.agentcore.service.JournalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsoleJournalService implements JournalService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleJournalService.class);

    @Override
    public void recordDecisionContext(DecisionContext context) {
        log.info(
                "Decision context: ticker={}, time={}, candles={}, cash={}, totalValue={}",
                context.ticker(),
                context.time(),
                context.marketSnapshot().candles().size(),
                context.portfolioState().cash(),
                context.portfolioState().totalValue()
        );
    }

    @Override
    public void recordTradeDecision(TradeDecision decision) {
        log.info(
                "Trade decision: ticker={}, action={}, quantity={}, confidence={}, reason={}",
                decision.ticker(),
                decision.action(),
                decision.quantity(),
                decision.confidence(),
                decision.reason()
        );
    }

    @Override
    public void recordRiskCheck(RiskCheckResult result) {
        log.info(
                "Risk check: approved={}, finalAction={}, reason={}",
                result.approved(),
                result.finalAction(),
                result.reason()
        );
    }

    @Override
    public void recordOrderResult(OrderResult result) {
        log.info(
                "Order result: ticker={}, side={}, quantity={}, requestedPrice={}, executedPrice={}, status={}, message={}",
                result.ticker(),
                result.side(),
                result.quantity(),
                result.requestedPrice(),
                result.executedPrice(),
                result.status(),
                result.message()
        );
    }
}
