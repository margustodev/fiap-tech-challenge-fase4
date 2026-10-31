
package br.com.fiap.techchallenge.fase4.report.config;

import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.application.usecase.GerarRelatorioSemanalUseCase;
import br.com.fiap.techchallenge.fase4.infrastructure.config.AppConfig;
import br.com.fiap.techchallenge.fase4.infrastructure.persistence.DynamoDbAvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.infrastructure.persistence.DynamoDbClientFactory;
import br.com.fiap.techchallenge.fase4.report.infrastructure.email.SesEmailService;
import br.com.fiap.techchallenge.fase4.report.infrastructure.report.SesRelatorioService;

/**
 * Configuração e montagem das dependências
 * da função Lambda weekly-reporter.
 */
public class WeeklyReporterConfig {

    private final AppConfig appConfig = new AppConfig();

    private final DynamoDbClientFactory clientFactory =
            new DynamoDbClientFactory(appConfig);

    public AppConfig appConfig() {
        return appConfig;
    }

    public AvaliacaoRepository avaliacaoRepository() {

        return new DynamoDbAvaliacaoRepository(
                clientFactory.createEnhancedClient(),
                appConfig
        );
    }

    public GerarRelatorioSemanalUseCase gerarRelatorioSemanalUseCase() {

        // Serviço responsável pelo envio via Amazon SES.
        SesEmailService emailService =
                new SesEmailService();

        // Serviço responsável por formatar e entregar o relatório.
        SesRelatorioService relatorioService =
                new SesRelatorioService(
                        emailService,
                        appConfig.getEmailAdministradores()
                );

        // Caso de uso que consulta o DynamoDB,
        // calcula as estatísticas e envia o relatório.
        return new GerarRelatorioSemanalUseCase(
                avaliacaoRepository(),
                relatorioService
        );
    }
}
