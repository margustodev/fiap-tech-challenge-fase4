
package br.com.fiap.techchallenge.fase4.report.infrastructure.report;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResumoDto;
import br.com.fiap.techchallenge.fase4.application.dto.RelatorioSemanalDto;
import br.com.fiap.techchallenge.fase4.application.port.out.EmailService;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorDia;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorUrgencia;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SesRelatorioServiceTest {

    private final EmailServiceFake emailFake =
            new EmailServiceFake();

    private final SesRelatorioService service =
            new SesRelatorioService(
                    emailFake,
                    "admin@exemplo.com"
            );

    @Test
    void deveFormatarEEnviarRelatorioSemanal() {

        RelatorioSemanalDto relatorio =
                new RelatorioSemanalDto(
                        LocalDate.of(2026, 10, 19),
                        LocalDate.of(2026, 10, 25),
                        5.0,
                        2,
                        List.of(
                                new AvaliacaoResumoDto(
                                        "Curso precisa melhorar",
                                        Urgencia.CRITICA,
                                        Instant.parse(
                                                "2026-10-20T10:00:00Z"
                                        )
                                ),
                                new AvaliacaoResumoDto(
                                        "Curso muito bom",
                                        Urgencia.BAIXA,
                                        Instant.parse(
                                                "2026-10-20T15:00:00Z"
                                        )
                                )
                        ),
                        List.of(
                                new EstatisticaPorDia(
                                        LocalDate.of(2026, 10, 20),
                                        2L
                                )
                        ),
                        List.of(
                                new EstatisticaPorUrgencia(
                                        Urgencia.CRITICA,
                                        1L
                                ),
                                new EstatisticaPorUrgencia(
                                        Urgencia.BAIXA,
                                        1L
                                )
                        )
                );

        service.enviarRelatorio(relatorio);

        assertEquals(1, emailFake.quantidadeEnvios);

        assertEquals(
                "admin@exemplo.com",
                emailFake.destinatario
        );

        assertEquals(
                "Relatório Semanal de Avaliações - "
                        + "19/10/2026 a 25/10/2026",
                emailFake.assunto
        );

        assertNotNull(emailFake.corpo);

        assertTrue(emailFake.corpo.contains(
                "Total de avaliações: 2"
        ));

        assertTrue(emailFake.corpo.contains(
                "Média das avaliações: 5,00"
        ));

        assertTrue(emailFake.corpo.contains(
                "20/10/2026: 2 avaliação(ões)"
        ));

        assertTrue(emailFake.corpo.contains(
                "CRITICA: 1"
        ));

        assertTrue(emailFake.corpo.contains(
                "BAIXA: 1"
        ));

        assertTrue(emailFake.corpo.contains(
                "Curso precisa melhorar"
        ));

        assertTrue(emailFake.corpo.contains(
                "Curso muito bom"
        ));

        assertTrue(emailFake.corpo.contains(
                "20/10/2026 10:00 UTC"
        ));
    }

    @Test
    void deveEnviarRelatorioMesmoSemAvaliacoes() {

        RelatorioSemanalDto relatorio =
                new RelatorioSemanalDto(
                        LocalDate.of(2026, 10, 19),
                        LocalDate.of(2026, 10, 25),
                        0.0,
                        0,
                        List.of(),
                        List.of(),
                        List.of()
                );

        service.enviarRelatorio(relatorio);

        assertEquals(1, emailFake.quantidadeEnvios);

        assertTrue(emailFake.corpo.contains(
                "Total de avaliações: 0"
        ));

        assertTrue(emailFake.corpo.contains(
                "Média das avaliações: 0,00"
        ));

        assertTrue(emailFake.corpo.contains(
                "Nenhuma avaliação recebida no período."
        ));

        assertTrue(emailFake.corpo.contains("CRITICA: 0"));
        assertTrue(emailFake.corpo.contains("ALTA: 0"));
        assertTrue(emailFake.corpo.contains("MEDIA: 0"));
        assertTrue(emailFake.corpo.contains("BAIXA: 0"));
    }

    @Test
    void deveRejeitarRelatorioNulo() {

        assertThrows(
                NullPointerException.class,
                () -> service.enviarRelatorio(null)
        );

        assertEquals(0, emailFake.quantidadeEnvios);
    }

    /**
     * Simula o serviço de e-mail, sem
     * realizar chamadas reais ao Amazon SES.
     */
    private static class EmailServiceFake
            implements EmailService {

        private String destinatario;
        private String assunto;
        private String corpo;
        private int quantidadeEnvios;

        @Override
        public void enviar(
                String destinatario,
                String assunto,
                String corpo) {

            this.destinatario = destinatario;
            this.assunto = assunto;
            this.corpo = corpo;
            this.quantidadeEnvios++;
        }
    }
}
