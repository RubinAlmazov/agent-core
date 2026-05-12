package org.me.agentcore.service.fake;

import org.me.agentcore.domain.OrderRequest;
import org.me.agentcore.domain.OrderResult;
import org.me.agentcore.domain.OrderStatus;
import org.me.agentcore.service.BrokerService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DryRunBrokerService implements BrokerService {

    @Override
    public OrderResult placeOrder(OrderRequest request) {
        return new OrderResult(
                request.ticker(),
                request.side(),
                request.quantity(),
                request.price(),
                request.price(),
                OrderStatus.FILLED,
                Instant.now(),
                "Dry-run order filled"
        );
    }
}
