package br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.exception.NotificacaoException;
import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

/**
 * Publica alertas de urgência no tópico Amazon SNS dos administradores.
 */
public class SnsNotificacaoService implements NotificacaoService {

    private static final Logger logger =
            LoggerFactory.getLogger(SnsNotificacaoService.class);

    @FunctionalInterface
    public interface TopicPublisher {
        String publish(String topicArn, String subject, String message);
    }

    private final String topicArn;
    private final TopicPublisher publisher;

    public SnsNotificacaoService() {
        this(System.getenv("SNS_TOPIC_ARN"), SnsNotificacaoService::publishWithSdk);
    }

    public SnsNotificacaoService(String topicArn, TopicPublisher publisher) {
        if (topicArn == null || topicArn.isBlank()) {
            throw new IllegalStateException(
                    "Tópico SNS não configurado: SNS_TOPIC_ARN."
            );
        }
        if (publisher == null) {
            throw new IllegalArgumentException("Publisher SNS não informado.");
        }
        this.topicArn = topicArn;
        this.publisher = publisher;
    }

    @Override
    public void notificarUrgencia(NotificacaoUrgenciaDto notificacao) {
        if (notificacao == null
                || notificacao.getDescricao() == null
                || notificacao.getUrgencia() == null
                || notificacao.getDataEnvio() == null) {
            throw new IllegalArgumentException("Dados da notificação inválidos.");
        }

        String subject = "Alerta de avaliacao - Urgencia "
                + notificacao.getUrgencia();

        String message = """
                Uma nova avaliacao precisa de atencao.

                Descricao: %s
                Urgencia: %s
                Data de envio (UTC): %s

                Mensagem automatica do sistema de avaliacoes.
                """.formatted(
                notificacao.getDescricao(),
                notificacao.getUrgencia(),
                notificacao.getDataEnvio()
        );

        logger.info(
                "Publicando alerta de urgência no SNS: {}",
                notificacao.getUrgencia()
        );

        try {
            String messageId = publisher.publish(topicArn, subject, message);
            logger.info("Alerta SNS publicado. MessageId: {}", messageId);
        } catch (RuntimeException e) {
            throw new NotificacaoException(
                    "Falha ao publicar alerta de urgência no SNS.",
                    e
            );
        }
    }

    private static String publishWithSdk(
            String topicArn,
            String subject,
            String message) {

        try (SnsClient client = SnsClient.create()) {
            PublishResponse response = client.publish(
                    PublishRequest.builder()
                            .topicArn(topicArn)
                            .subject(subject)
                            .message(message)
                            .build()
            );
            return response.messageId();
        }
    }
}
