package org.me.agentcore;

import org.me.agentcore.agent.TradingAgent;
import org.me.agentcore.repository.PortfolioSnapshotJournalRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AgentCoreApplication {

    static TradingAgent tradingAgent;
    static PortfolioSnapshotJournalRepository portfolioSnapshotJournalRepository;

    public static void main(String[] args) {
        SpringApplication.run(AgentCoreApplication.class, args);

//        tradingAgent = context.getBean(TradingAgent.class);
//        portfolioSnapshotJournalRepository = context.getBean(PortfolioSnapshotJournalRepository.class);
//
//        while (true) {
//            System.out.println("Провожу запуск");
//            tradingAgent.runOnce("SBER");
//            System.out.println("Выполнил запуск");
//            System.out.println(portfolioSnapshotJournalRepository.findLatestSnapshotJson());
//            try {
//                Thread.sleep(10000);
//                System.out.println("Прошло 10 секунд");
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

}
