
package br.com.fiap.techchallenge.fase4.notifier.infrastructure.email;

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
 * Servico responsavel pelo envio de e-mails via Amazon SES.
 */
public class SesEmailService implements EmailService {

    private static final Logger logger =
            LoggerFactory.getLogger(SesEmailService.class);

    @Override
    public void enviar(String destinatario, String assunto, String corpo) {

        String remetente = System.getenv("SENDER_EMAIL");

        if (remetente == null || remetente.isBlank()) {
            throw new IllegalStateException(
                    "Remetente nao configurado: SENDER_EMAIL."
            );
        }

        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException(
                    "Destinatario do e-mail nao informado."
            );
        }

        if (assunto == null || corpo == null) {
            throw new IllegalArgumentException(
                    "Assunto e corpo do e-mail sao obrigatorios."
            );
        }

        // Monta a mensagem
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

        // Monta a requisicao para o Amazon SES
        SendEmailRequest requisicao = SendEmailRequest.builder()
                .source(remetente)
                .destination(Destination.builder()
                        .toAddresses(destinatario)
                        .build())
                .message(mensagem)
                .build();

        // Envia o e-mail
        try (SesClient cliente = SesClient.create()) {

            SendEmailResponse resposta = cliente.sendEmail(requisicao);

            logger.info(
                    "E-mail enviado via SES. MessageId: {}",
                    resposta.messageId()
            );
        }
    }
}
