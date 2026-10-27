package br.com.fiap.techchallenge.fase4.report.config;

import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.application.usecase.GerarRelatorioSemanalUseCase;
import br.com.fiap.techchallenge.fase4.infrastructure.config.AppConfig;
import br.com.fiap.techchallenge.fase4.infrastructure.persistence.DynamoDbAvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.infrastructure.persistence.DynamoDbClientFactory;

/**
 * Montagem de dependências da função weekly-reporter.
 */
public class WeeklyReporterConfig {

    private final AppConfig appConfig = new AppConfig();
    private final DynamoDbClientFactory clientFactory = new DynamoDbClientFactory(appConfig);

    public AppConfig appConfig() {
        return appConfig;
    }

    public AvaliacaoRepository avaliacaoRepository() {
        return new DynamoDbAvaliacaoRepository(clientFactory.createEnhancedClient(), appConfig);
    }

    public GerarRelatorioSemanalUseCase gerarRelatorioSemanalUseCase() {
        // TODO: instanciar agregadores, e-mail e caso de uso com avaliacaoRepository()
        return null;
    }

}
