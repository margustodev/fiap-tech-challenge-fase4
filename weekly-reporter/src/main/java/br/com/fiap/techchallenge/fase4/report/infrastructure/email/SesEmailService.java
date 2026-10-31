
package br.com.fiap.techchallenge.fase4.report.infrastructure.email;

import br.com.fiap.techchallenge.fase4.application.port.out.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

/**
 * Adaptador responsável por enviar e-mails
 * do relatório semanal utilizando Amazon SES.
 */
public class SesEmailService implements EmailService {

    private static final Logger logger =
            LoggerFactory.getLogger(SesEmailService.class);

    @Override
    public void enviar(
            String destinatario,
            String assunto,
            String corpo) {

        String remetente = System.getenv("SENDER_EMAIL");

        if (remetente == null || remetente.isBlank()) {
            throw new IllegalStateException(
                    "Remetente não configurado: SENDER_EMAIL."
            );
        }

        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException(
                    "Destinatário não informado."
            );
        }

        if (assunto == null || assunto.isBlank()) {
            throw new IllegalArgumentException(
                    "Assunto do e-mail não informado."
            );
        }

        if (corpo == null || corpo.isBlank()) {
            throw new IllegalArgumentException(
                    "Corpo do e-mail não informado."
            );
        }

        Message mensagem = Message.builder()
                .subject(Content.builder()
                        .data(assunto)
                        .charset("UTF-8")
                        .build())
                .body(Body.builder()
                        .text(Content.builder()
                                .data(corpo)
                                .charset("UTF-8")
                                .build())
                        .build())
                .build();

        SendEmailRequest requisicao = SendEmailRequest.builder()
                .source(remetente)
                .destination(Destination.builder()
                        .toAddresses(destinatario)
                        .build())
                .message(mensagem)
                .build();

        try (SesClient cliente = SesClient.create()) {

            SendEmailResponse resposta =
                    cliente.sendEmail(requisicao);

            logger.info(
                    "Relatório semanal enviado via SES. MessageId: {}",
                    resposta.messageId()
            );
        }
    }
}
