
package br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.port.out.EmailService;
import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;
import br.com.fiap.techchallenge.fase4.notifier.infrastructure.email.SesEmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prepara e envia notificacoes de urgencia aos administradores.
 */
public class SesNotificacaoService implements NotificacaoService {

    private static final Logger logger =
            LoggerFactory.getLogger(SesNotificacaoService.class);

    private final EmailService emailService;
    private final String destinatario;

    // Utilizado pela aplicacao na AWS
    public SesNotificacaoService() {
        this(new SesEmailService(), System.getenv("ADMIN_EMAIL"));
    }

    // Permite informar um servico de e-mail alternativo
    public SesNotificacaoService(EmailService emailService) {
        this(emailService, System.getenv("ADMIN_EMAIL"));
    }

    // Permite simular o servico e o destinatario nos testes
    public SesNotificacaoService(
            EmailService emailService,
            String destinatario) {

        if (emailService == null) {
            throw new IllegalArgumentException(
                    "Servico de e-mail nao informado."
            );
        }

        this.emailService = emailService;
        this.destinatario = destinatario;
    }

    @Override
    public void notificarUrgencia(NotificacaoUrgenciaDto notificacao) {

        if (notificacao == null ||
                notificacao.getDescricao() == null ||
                notificacao.getUrgencia() == null ||
                notificacao.getDataEnvio() == null) {

            throw new IllegalArgumentException(
                    "Dados da notificacao invalidos."
            );
        }

        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalStateException(
                    "E-mail do administrador nao configurado."
            );
        }

        String assunto = "Alerta de avaliacao - Urgencia "
                + notificacao.getUrgencia();

        String corpo = """
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
                "Preparando notificacao de urgencia: {}",
                notificacao.getUrgencia()
        );

        emailService.enviar(destinatario, assunto, corpo);

        logger.info("Solicitacao de notificacao concluida.");
    }
}
