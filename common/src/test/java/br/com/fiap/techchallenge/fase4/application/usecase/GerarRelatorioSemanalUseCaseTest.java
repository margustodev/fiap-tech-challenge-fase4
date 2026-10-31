
package br.com.fiap.techchallenge.fase4.application.usecase;

import br.com.fiap.techchallenge.fase4.application.dto.RelatorioSemanalDto;
import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.application.port.out.RelatorioService;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorUrgencia;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GerarRelatorioSemanalUseCaseTest {

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-10-29T12:00:00Z"),
            ZoneOffset.UTC
    );

    @Test
    void deveGerarEEnviarRelatorioDaSemanaAnterior() {

        List<Avaliacao> avaliacoes = List.of(
                criarAvaliacao(
                        "1", 2, Urgencia.CRITICA,
                        "2026-10-19T10:00:00Z"
                ),
                criarAvaliacao(
                        "2", 8, Urgencia.BAIXA,
                        "2026-10-19T15:00:00Z"
                ),
                criarAvaliacao(
                        "3", 6, Urgencia.MEDIA,
                        "2026-10-25T23:59:59Z"
                )
        );

        RepositorioFake repositorio =
                new RepositorioFake(avaliacoes);

        RelatorioServiceFake servico =
                new RelatorioServiceFake();

        GerarRelatorioSemanalUseCase useCase =
                new GerarRelatorioSemanalUseCase(
                        repositorio,
                        servico,
                        clock
                );

        RelatorioSemanalDto resultado = useCase.executar();

        assertNotNull(resultado);

        assertEquals(
                LocalDate.of(2026, 10, 19),
                resultado.getDataInicio()
        );

        assertEquals(
                LocalDate.of(2026, 10, 25),
                resultado.getDataFim()
        );

        assertEquals(3, resultado.getTotalAvaliacoes());

        assertEquals(
                16.0 / 3,
                resultado.getMediaAvaliacoes(),
                0.0001
        );

        assertEquals(3, resultado.getAvaliacoes().size());

        assertEquals(2, resultado.getQuantidadePorDia().size());

        assertEquals(
                2L,
                resultado.getQuantidadePorDia()
                        .get(0).getQuantidade()
        );

        assertEquals(
                1L,
                resultado.getQuantidadePorDia()
                        .get(1).getQuantidade()
        );

        assertEquals(
                1L,
                quantidadeDe(
                        resultado,
                        Urgencia.CRITICA
                )
        );

        assertEquals(
                1L,
                quantidadeDe(
                        resultado,
                        Urgencia.MEDIA
                )
        );

        assertEquals(
                1L,
                quantidadeDe(
                        resultado,
                        Urgencia.BAIXA
                )
        );

        assertEquals(
                0L,
                quantidadeDe(
                        resultado,
                        Urgencia.ALTA
                )
        );

        assertEquals(1, servico.quantidadeEnvios);
        assertSame(resultado, servico.relatorioEnviado);
    }

    @Test
    void deveExcluirAvaliacoesDaSemanaAtual() {

        List<Avaliacao> avaliacoes = List.of(
                criarAvaliacao(
                        "1", 7, Urgencia.MEDIA,
                        "2026-10-25T23:59:59Z"
                ),
                criarAvaliacao(
                        "2", 9, Urgencia.BAIXA,
                        "2026-10-26T00:00:00Z"
                )
        );

        RepositorioFake repositorio =
                new RepositorioFake(avaliacoes);

        RelatorioServiceFake servico =
                new RelatorioServiceFake();

        GerarRelatorioSemanalUseCase useCase =
                new GerarRelatorioSemanalUseCase(
                        repositorio,
                        servico,
                        clock
                );

        RelatorioSemanalDto resultado = useCase.executar();

        assertEquals(
                Instant.parse("2026-10-19T00:00:00Z"),
                repositorio.inicioConsultado
        );

        assertEquals(
                Instant.parse("2026-10-26T00:00:00Z"),
                repositorio.fimConsultado
        );

        assertEquals(1, resultado.getTotalAvaliacoes());
        assertEquals(7.0, resultado.getMediaAvaliacoes(), 0.0001);
        assertEquals(1, servico.quantidadeEnvios);
    }

    @Test
    void deveGerarRelatorioMesmoSemAvaliacoes() {

        RepositorioFake repositorio =
                new RepositorioFake(List.of());

        RelatorioServiceFake servico =
                new RelatorioServiceFake();

        GerarRelatorioSemanalUseCase useCase =
                new GerarRelatorioSemanalUseCase(
                        repositorio,
                        servico,
                        clock
                );

        RelatorioSemanalDto resultado = useCase.executar();

        assertNotNull(resultado);
        assertEquals(0, resultado.getTotalAvaliacoes());
        assertEquals(0.0, resultado.getMediaAvaliacoes(), 0.0001);
        assertTrue(resultado.getAvaliacoes().isEmpty());
        assertTrue(resultado.getQuantidadePorDia().isEmpty());
        assertTrue(resultado.getQuantidadePorUrgencia().isEmpty());

        assertEquals(1, servico.quantidadeEnvios);
        assertSame(resultado, servico.relatorioEnviado);
    }

    private Long quantidadeDe(
            RelatorioSemanalDto relatorio,
            Urgencia urgencia) {

        return relatorio.getQuantidadePorUrgencia()
                .stream()
                .filter(item -> item.getUrgencia() == urgencia)
                .findFirst()
                .map(EstatisticaPorUrgencia::getQuantidade)
                .orElseThrow();
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

    private static class RepositorioFake
            implements AvaliacaoRepository {

        private final List<Avaliacao> avaliacoes;
        private Instant inicioConsultado;
        private Instant fimConsultado;

        RepositorioFake(List<Avaliacao> avaliacoes) {
            this.avaliacoes = avaliacoes;
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

            this.inicioConsultado = inicio;
            this.fimConsultado = fim;

            return avaliacoes;
        }
    }

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
