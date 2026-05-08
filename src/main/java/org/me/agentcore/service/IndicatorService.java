package org.me.agentcore.service;

import org.me.agentcore.domain.IndicatorSnapshot;
import org.me.agentcore.domain.MarketSnapshot;

public interface IndicatorService {

    IndicatorSnapshot calculate(MarketSnapshot marketSnapshot);
}
