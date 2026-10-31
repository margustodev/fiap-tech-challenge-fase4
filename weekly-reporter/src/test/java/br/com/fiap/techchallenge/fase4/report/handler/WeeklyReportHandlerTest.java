
package br.com.fiap.techchallenge.fase4.report.handler;

import br.com.fiap.techchallenge.fase4.application.dto.RelatorioSemanalDto;
import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.application.port.out.RelatorioService;
import br.com.fiap.techchallenge.fase4.application.usecase.GerarRelatorioSemanalUseCase;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WeeklyReportHandlerTest {

    private static final Clock CLOCK_TESTE = Clock.fixed(
            Instant.parse("2026-10-29T12:00:00Z"),
            ZoneOffset.UTC
    );

    @Test
    void deveExecutarRelatorioQuandoLambdaForAcionada() {

        List<Avaliacao> avaliacoes = List.of(
                criarAvaliacao(
                        "1",
                        6,
                        Urgencia.MEDIA,
                        "2026-10-19T10:00:00Z"
                ),
                criarAvaliacao(
                        "2",
                        8,
                        Urgencia.BAIXA,
                        "2026-10-25T15:00:00Z"
                )
        );

        RepositorioFake repositorio =
                new RepositorioFake(avaliacoes);

        RelatorioServiceFake servico =
                new RelatorioServiceFake();

        WeeklyReportHandler handler =
                criarHandler(repositorio, servico);

        Void resposta = handler.handleRequest(
                new ScheduledEvent(),
                null
        );

        assertNull(resposta);

        assertEquals(1, repositorio.quantidadeConsultas);
        assertEquals(1, servico.quantidadeEnvios);

        assertNotNull(servico.relatorioEnviado);

        assertEquals(
                LocalDate.of(2026, 10, 19),
                servico.relatorioEnviado.getDataInicio()
        );

        assertEquals(
                LocalDate.of(2026, 10, 25),
                servico.relatorioEnviado.getDataFim()
        );

        assertEquals(
                2,
                servico.relatorioEnviado.getTotalAvaliacoes()
        );

        assertEquals(
                7.0,
                servico.relatorioEnviado.getMediaAvaliacoes(),
                0.0001
        );
    }

    @Test
    void deveExecutarLambdaMesmoSemAvaliacoes() {

        RepositorioFake repositorio =
                new RepositorioFake(List.of());

        RelatorioServiceFake servico =
                new RelatorioServiceFake();

        WeeklyReportHandler handler =
                criarHandler(repositorio, servico);

        Void resposta = handler.handleRequest(
                new ScheduledEvent(),
                null
        );

        assertNull(resposta);

        assertEquals(1, repositorio.quantidadeConsultas);
        assertEquals(1, servico.quantidadeEnvios);

        assertNotNull(servico.relatorioEnviado);

        assertEquals(
                0,
                servico.relatorioEnviado.getTotalAvaliacoes()
        );

        assertEquals(
                0.0,
                servico.relatorioEnviado.getMediaAvaliacoes(),
                0.0001
        );

        assertTrue(
                servico.relatorioEnviado.getAvaliacoes().isEmpty()
        );
    }

    @Test
    void devePropagarErroQuandoConsultaFalhar() {

        RepositorioFake repositorio =
                new RepositorioFake(List.of(), true);

        RelatorioServiceFake servico =
                new RelatorioServiceFake();

        WeeklyReportHandler handler =
                criarHandler(repositorio, servico);

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> handler.handleRequest(
                        new ScheduledEvent(),
                        null
                )
        );

        assertEquals(
                "Falha simulada na consulta ao DynamoDB",
                erro.getMessage()
        );

        assertEquals(1, repositorio.quantidadeConsultas);
        assertEquals(0, servico.quantidadeEnvios);
        assertNull(servico.relatorioEnviado);
    }

    private WeeklyReportHandler criarHandler(
            AvaliacaoRepository repositorio,
            RelatorioService servico) {

        GerarRelatorioSemanalUseCase useCase =
                new GerarRelatorioSemanalUseCase(
                        repositorio,
                        servico,
                        CLOCK_TESTE
                );

        return new WeeklyReportHandler(useCase);
    }

    private Avaliacao criarAvaliacao(
            String id,
            Integer nota,
            Urgencia urgencia,
            String dataEnvio) {

        return new Avaliacao(
                id,
                "Avaliação de teste",
                nota,
                urgencia,
                Instant.parse(dataEnvio)
        );
    }

    /**
     * Repositório simulado para evitar
     * consultas reais ao DynamoDB.
     */
    private static class RepositorioFake
            implements AvaliacaoRepository {

        private final List<Avaliacao> avaliacoes;
        private final boolean simularFalha;

        private int quantidadeConsultas;

        RepositorioFake(List<Avaliacao> avaliacoes) {
            this(avaliacoes, false);
        }

        RepositorioFake(
                List<Avaliacao> avaliacoes,
                boolean simularFalha) {

            this.avaliacoes = avaliacoes;
            this.simularFalha = simularFalha;
        }

        @Override
        public Avaliacao salvar(Avaliacao avaliacao) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Avaliacao> buscarPorId(String id) {
            return Optional.empty();
        }

        @Override
        public List<Avaliacao> listarPorPeriodo(
                Instant inicio,
                Instant fim) {

            quantidadeConsultas++;

            if (simularFalha) {
                throw new IllegalStateException(
                        "Falha simulada na consulta ao DynamoDB"
                );
            }

            return avaliacoes;
        }
    }

    /**
     * Simula a entrega do relatório,
     * sem enviar e-mails reais.
     */
    private static class RelatorioServiceFake
            implements RelatorioService {

        private RelatorioSemanalDto relatorioEnviado;
        private int quantidadeEnvios;

        @Override
        public void enviarRelatorio(
                RelatorioSemanalDto relatorio) {

            this.relatorioEnviado = relatorio;
            this.quantidadeEnvios++;
        }
    }
}
