package org.me.agentcore.controller;

import org.me.agentcore.repository.DecisionJournalRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/decisions")
public class DecisionController {

    private static final int DEFAULT_LIMIT = 20;

    private final DecisionJournalRepository decisionJournalRepository;
    private final ObjectMapper objectMapper;

    public DecisionController(DecisionJournalRepository decisionJournalRepository, ObjectMapper objectMapper) {
        this.decisionJournalRepository = decisionJournalRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getLatestDecisions() {
        List<JsonNode> decisions = decisionJournalRepository.findLatestDecisionJson(DEFAULT_LIMIT).stream()
                .map(objectMapper::readTree)
                .toList();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(decisions));
    }
}
