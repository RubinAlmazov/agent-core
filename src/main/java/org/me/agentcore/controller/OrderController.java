package org.me.agentcore.controller;

import org.me.agentcore.repository.OrderJournalRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final int DEFAULT_LIMIT = 20;

    private final OrderJournalRepository orderJournalRepository;
    private final ObjectMapper objectMapper;

    public OrderController(OrderJournalRepository orderJournalRepository, ObjectMapper objectMapper) {
        this.orderJournalRepository = orderJournalRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getLatestOrders() {
        List<JsonNode> orders = orderJournalRepository.findLatestOrderJson(DEFAULT_LIMIT).stream()
                .map(objectMapper::readTree)
                .toList();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(orders));
    }
}
