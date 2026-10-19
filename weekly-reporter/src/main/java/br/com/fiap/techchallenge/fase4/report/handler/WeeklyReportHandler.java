package br.com.fiap.techchallenge.fase4.report.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

/**
 * Handler Lambda: acionado semanalmente pelo Amazon EventBridge.
 * Responsabilidade única: gerar e enviar o relatório semanal de feedbacks.
 */
public class WeeklyReportHandler implements RequestHandler<ScheduledEvent, Void> {

    @Override
    public Void handleRequest(ScheduledEvent event, Context context) {
        // TODO: executar GerarRelatorioSemanalUseCase
        return null;
    }

}
