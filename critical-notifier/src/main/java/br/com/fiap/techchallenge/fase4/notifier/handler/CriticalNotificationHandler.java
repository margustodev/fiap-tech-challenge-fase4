package br.com.fiap.techchallenge.fase4.notifier.handler;

import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;
import br.com.fiap.techchallenge.fase4.application.usecase.ProcessarNotificacaoCriticaUseCase;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.infrastructure.security.DataPrivacyHelper;
import br.com.fiap.techchallenge.fase4.notifier.config.CriticalNotifierConfig;
import br.com.fiap.techchallenge.fase4.notifier.infrastructure.mapper.DynamoDbStreamMapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lambda responsável por identificar avaliações críticas
 * e solicitar notificações aos administradores (SES e SNS).
 */
public class CriticalNotificationHandler
        implements RequestHandler<DynamodbEvent, Void> {

    private static final Logger logger =
            LoggerFactory.getLogger(CriticalNotificationHandler.class);

    private final DynamoDbStreamMapper mapper;
    private final ProcessarNotificacaoCriticaUseCase useCase;
    private final DataPrivacyHelper dataPrivacyHelper;

    public CriticalNotificationHandler() {
        CriticalNotifierConfig config = new CriticalNotifierConfig();
        this.mapper = config.dynamoDbStreamMapper();
        this.useCase = config.processarNotificacaoCriticaUseCase();
        this.dataPrivacyHelper = new DataPrivacyHelper();
    }

    public CriticalNotificationHandler(
            DynamoDbStreamMapper mapper,
            NotificacaoService notificacaoService) {
        this(
                mapper,
                new ProcessarNotificacaoCriticaUseCase(notificacaoService),
                new DataPrivacyHelper()
        );
    }

    public CriticalNotificationHandler(
            DynamoDbStreamMapper mapper,
            ProcessarNotificacaoCriticaUseCase useCase,
            DataPrivacyHelper dataPrivacyHelper) {
        this.mapper = mapper;
        this.useCase = useCase;
        this.dataPrivacyHelper = dataPrivacyHelper;
    }

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        logger.info("Iniciando processamento das avaliacoes.");

        if (event == null || event.getRecords() == null) {
            logger.warn("Nenhum registro recebido do DynamoDB.");
            return null;
        }

        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
            if (record == null || !"INSERT".equals(record.getEventName())) {
                continue;
            }

            Avaliacao avaliacao = mapper.toAvaliacao(record);
            boolean notificado = useCase.executar(avaliacao);

            if (notificado) {
                logger.info(
                        "Notificacao processada para avaliacao ID: {}, urgencia: {}, descricao: {}",
                        avaliacao.getId(),
                        avaliacao.getUrgencia(),
                        dataPrivacyHelper.mascararDescricao(avaliacao.getDescricao())
                );
            } else {
                logger.info(
                        "Avaliacao sem necessidade de notificacao. ID: {}",
                        avaliacao.getId()
                );
            }
        }

        logger.info("Processamento das avaliacoes finalizado.");
        return null;
    }
}
