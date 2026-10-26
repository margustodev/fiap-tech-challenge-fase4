
package br.com.fiap.techchallenge.fase4.notifier.handler;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.notifier.infrastructure.mapper.DynamoDbStreamMapper;
import br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification.SesNotificacaoService;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lambda responsavel por identificar avaliacoes criticas
 * e solicitar notificacoes aos administradores.
 */
public class CriticalNotificationHandler
        implements RequestHandler<DynamodbEvent, Void> {

    private static final Logger logger =
            LoggerFactory.getLogger(CriticalNotificationHandler.class);

    private final DynamoDbStreamMapper mapper;
    private final NotificacaoService notificacaoService;

    public CriticalNotificationHandler() {
        this(new DynamoDbStreamMapper(), new SesNotificacaoService());
    }

    public CriticalNotificationHandler(
            DynamoDbStreamMapper mapper,
            NotificacaoService notificacaoService) {

        this.mapper = mapper;
        this.notificacaoService = notificacaoService;
    }

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {

        logger.info("Iniciando processamento das avaliacoes.");

        if (event == null || event.getRecords() == null) {
            logger.warn("Nenhum registro recebido do DynamoDB.");
            return null;
        }

        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {

            // Processa apenas registros de novas avaliacoes
            if (record == null || !"INSERT".equals(record.getEventName())) {
                continue;
            }

            // Converte os dados recebidos do DynamoDB
            Avaliacao avaliacao = mapper.toAvaliacao(record);

            Urgencia urgencia = avaliacao.getUrgencia();

            if (urgencia == Urgencia.ALTA || urgencia == Urgencia.CRITICA) {

                logger.info(
                        "Avaliacao com urgencia identificada. ID: {}, Urgencia: {}",
                        avaliacao.getId(),
                        urgencia
                );

                // Prepara os dados da notificacao
                NotificacaoUrgenciaDto notificacao =
                        new NotificacaoUrgenciaDto(
                                avaliacao.getDescricao(),
                                urgencia,
                                avaliacao.getDataEnvio()
                        );

                // Solicita o envio do e-mail
                notificacaoService.notificarUrgencia(notificacao);

                logger.info(
                        "Notificacao processada para avaliacao ID: {}",
                        avaliacao.getId()
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
