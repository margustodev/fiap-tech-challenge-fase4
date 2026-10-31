
package br.com.fiap.techchallenge.fase4.report.handler;

import br.com.fiap.techchallenge.fase4.application.dto.RelatorioSemanalDto;
import br.com.fiap.techchallenge.fase4.application.usecase.GerarRelatorioSemanalUseCase;
import br.com.fiap.techchallenge.fase4.report.config.WeeklyReporterConfig;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Handler da AWS Lambda responsável por executar
 * a geração do relatório semanal de avaliações.
 *
 * A execução é agendada pelo Amazon EventBridge.
 */
public class WeeklyReportHandler
        implements RequestHandler<ScheduledEvent, Void> {

    private static final Logger logger =
            LoggerFactory.getLogger(WeeklyReportHandler.class);

    private final GerarRelatorioSemanalUseCase useCase;

    /**
     * Construtor utilizado pela AWS Lambda.
     */
    public WeeklyReportHandler() {
        this(
                new WeeklyReporterConfig()
                        .gerarRelatorioSemanalUseCase()
        );
    }

    /**
     * Construtor que permite injetar o caso de uso,
     * facilitando os testes automatizados.
     */
    public WeeklyReportHandler(
            GerarRelatorioSemanalUseCase useCase) {

        this.useCase = Objects.requireNonNull(
                useCase,
                "O caso de uso é obrigatório."
        );
    }

    @Override
    public Void handleRequest(
            ScheduledEvent event,
            Context context) {

        logger.info(
                "Iniciando geração do relatório semanal."
        );

        RelatorioSemanalDto relatorio =
                useCase.executar();

        logger.info(
                "Relatório semanal processado. " +
                        "Período: {} a {}. Total de avaliações: {}.",
                relatorio.getDataInicio(),
                relatorio.getDataFim(),
                relatorio.getTotalAvaliacoes()
        );

        return null;
    }
}
