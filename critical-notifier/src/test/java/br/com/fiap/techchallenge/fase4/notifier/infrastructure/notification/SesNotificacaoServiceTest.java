
package br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.port.out.EmailService;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitarios do servico de notificacoes.
 */
class SesNotificacaoServiceTest {

    @Test
    void deveMontarEmailComDadosDaAvaliacaoCritica() {

        // Cria os dados da notificacao
        NotificacaoUrgenciaDto notificacao =
                new NotificacaoUrgenciaDto(
                        "Curso com conteudo desatualizado",
                        Urgencia.CRITICA,
                        Instant.parse("2026-10-19T18:00:00Z")
                );

        // Simula o servico de e-mail
        EmailSimulado emailSimulado = new EmailSimulado();

        SesNotificacaoService servico =
                new SesNotificacaoService(
                        emailSimulado,
                        "admin@exemplo.com"
                );

        // Executa a notificacao
        servico.notificarUrgencia(notificacao);

        // Confere o destinatario
        assertEquals(
                "admin@exemplo.com",
                emailSimulado.destinatario
        );

        // Confere o assunto
        assertEquals(
                "Alerta de avaliacao - Urgencia CRITICA",
                emailSimulado.assunto
        );

        // Confere o corpo do e-mail
        assertTrue(
                emailSimulado.corpo.contains(
                        "Curso com conteudo desatualizado"
                )
        );

        assertTrue(
                emailSimulado.corpo.contains("CRITICA")
        );

        assertTrue(
                emailSimulado.corpo.contains(
                        "2026-10-19T18:00:00Z"
                )
        );
    }

    @Test
    void devePropagarErroQuandoEnvioDoEmailFalhar() {

        // Cria uma notificacao critica
        NotificacaoUrgenciaDto notificacao =
                new NotificacaoUrgenciaDto(
                        "Problema grave no curso",
                        Urgencia.CRITICA,
                        Instant.parse("2026-10-19T18:00:00Z")
                );

        // Simula uma falha no servico de e-mail
        EmailService emailComFalha =
                (destinatario, assunto, corpo) -> {
                    throw new IllegalStateException(
                            "Falha simulada no envio."
                    );
                };

        SesNotificacaoService servico =
                new SesNotificacaoService(
                        emailComFalha,
                        "admin@exemplo.com"
                );

        // Verifica se a falha e propagada
        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> servico.notificarUrgencia(notificacao)
        );

        assertEquals(
                "Falha simulada no envio.",
                erro.getMessage()
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
