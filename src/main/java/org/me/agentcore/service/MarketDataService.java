package org.me.agentcore.service;

import org.me.agentcore.domain.MarketSnapshot;

public interface MarketDataService {

    MarketSnapshot getMarketSnapshot(String ticker);
}
