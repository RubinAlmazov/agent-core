package org.me.agentcore.service.llm;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.DecisionResult;
import org.me.agentcore.domain.TradeDecision;
import org.me.agentcore.service.DecisionService;
import org.springframework.stereotype.Service;

@Service
public class GroqDecisionService implements DecisionService {

    private final TradingPromptBuilder tradingPromptBuilder;
    private final GroqClient groqClient;
    private final LlmResponseParser llmResponseParser;

    public GroqDecisionService(
            TradingPromptBuilder tradingPromptBuilder,
            GroqClient groqClient,
            LlmResponseParser llmResponseParser
    ) {
        this.tradingPromptBuilder = tradingPromptBuilder;
        this.groqClient = groqClient;
        this.llmResponseParser = llmResponseParser;
    }

    @Override
    public DecisionResult decide(DecisionContext context) {
        String prompt = tradingPromptBuilder.buildPrompt(context);
        String rawLlmResponse = groqClient.requestDecision(prompt);
        TradeDecision tradeDecision = llmResponseParser.parse(context.ticker(), rawLlmResponse);

        return new DecisionResult(tradeDecision, prompt, rawLlmResponse);
    }
}
