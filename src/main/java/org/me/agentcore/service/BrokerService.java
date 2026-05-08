package org.me.agentcore.service;

import org.me.agentcore.domain.OrderRequest;
import org.me.agentcore.domain.OrderResult;

public interface BrokerService {

    OrderResult placeOrder(OrderRequest request);
}
