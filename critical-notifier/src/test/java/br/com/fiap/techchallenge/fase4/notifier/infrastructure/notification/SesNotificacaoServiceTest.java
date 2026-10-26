
package br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.port.out.EmailService;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitarios do servico de notificacoes via SES.
 */
class SesNotificacaoServiceTest {

    @Test
    void deveMontarEmailComDadosDaAvaliacaoCritica() {

        // 1. Cria os dados da notificacao
        NotificacaoUrgenciaDto notificacao =
                new NotificacaoUrgenciaDto(
                        "Curso com conteudo desatualizado",
                        Urgencia.CRITICA,
                        Instant.parse("2026-10-19T18:00:00Z")
                );

        // 2. Cria um servico de e-mail simulado
        EmailSimulado emailSimulado = new EmailSimulado();

        // 3. Cria o servico de notificacao
        SesNotificacaoService servico =
                new SesNotificacaoService(
                        emailSimulado,
                        "admin@exemplo.com"
                );

        // 4. Executa a notificacao
        servico.notificarUrgencia(notificacao);

        // 5. Verifica o destinatario
        assertEquals(
                "admin@exemplo.com",
                emailSimulado.destinatario
        );

        // 6. Verifica o assunto
        assertEquals(
                "Alerta de avaliacao - Urgencia CRITICA",
                emailSimulado.assunto
        );

        // 7. Verifica a descricao no corpo do e-mail
        assertTrue(
                emailSimulado.corpo.contains(
                        "Curso com conteudo desatualizado"
                )
        );

        // 8. Verifica a urgencia
        assertTrue(
                emailSimulado.corpo.contains("CRITICA")
        );

        // 9. Verifica a data de envio
        assertTrue(
                emailSimulado.corpo.contains(
                        "2026-10-19T18:00:00Z"
                )
        );
    }

    /**
     * Simula o envio de e-mail sem acessar a AWS.
     */
    private static class EmailSimulado implements EmailService {

        private String destinatario;
        private String assunto;
        private String corpo;

        @Override
        public void enviar(
                String destinatario,
                String assunto,
                String corpo) {

            this.destinatario = destinatario;
            this.assunto = assunto;
            this.corpo = corpo;
        }
    }
}
