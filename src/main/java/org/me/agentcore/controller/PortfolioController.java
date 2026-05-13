package org.me.agentcore.controller;

import org.me.agentcore.repository.PortfolioSnapshotJournalRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioSnapshotJournalRepository portfolioSnapshotJournalRepository;

    public PortfolioController(PortfolioSnapshotJournalRepository portfolioSnapshotJournalRepository) {
        this.portfolioSnapshotJournalRepository = portfolioSnapshotJournalRepository;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPortfolio() {
        return portfolioSnapshotJournalRepository.findLatestSnapshotJson()
                .map(snapshotJson -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(snapshotJson))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
