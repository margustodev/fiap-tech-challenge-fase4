
package br.com.fiap.techchallenge.fase4.application.usecase;

import br.com.fiap.techchallenge.fase4.application.dto.RelatorioSemanalDto;
import br.com.fiap.techchallenge.fase4.application.mapper.RelatorioMapper;
import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.application.port.out.RelatorioService;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.model.RelatorioSemanal;
import br.com.fiap.techchallenge.fase4.domain.service.CalcularMediaAvaliacoesService;
import br.com.fiap.techchallenge.fase4.domain.service.CalcularQuantidadePorDiaService;
import br.com.fiap.techchallenge.fase4.domain.service.CalcularQuantidadePorUrgenciaService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;

import static java.time.DayOfWeek.MONDAY;

/**
 * Caso de uso responsável por gerar e enviar
 * o relatório semanal de avaliações.
 */
public class GerarRelatorioSemanalUseCase {

    private final AvaliacaoRepository avaliacaoRepository;
    private final RelatorioService relatorioService;
    private final Clock clock;

    private final CalcularMediaAvaliacoesService mediaService =
            new CalcularMediaAvaliacoesService();

    private final CalcularQuantidadePorDiaService diaService =
            new CalcularQuantidadePorDiaService();

    private final CalcularQuantidadePorUrgenciaService urgenciaService =
            new CalcularQuantidadePorUrgenciaService();

    private final RelatorioMapper relatorioMapper =
            new RelatorioMapper();

    public GerarRelatorioSemanalUseCase(
            AvaliacaoRepository avaliacaoRepository,
            RelatorioService relatorioService) {

        this(
                avaliacaoRepository,
                relatorioService,
                Clock.systemUTC()
        );
    }

    public GerarRelatorioSemanalUseCase(
            AvaliacaoRepository avaliacaoRepository,
            RelatorioService relatorioService,
            Clock clock) {

        this.avaliacaoRepository = Objects.requireNonNull(
                avaliacaoRepository
        );

        this.relatorioService = Objects.requireNonNull(
                relatorioService
        );

        this.clock = Objects.requireNonNull(clock);
    }

    public RelatorioSemanalDto executar() {

        // Identifica a última semana completa.
        LocalDate dataInicio = LocalDate.now(
                        clock.withZone(ZoneOffset.UTC)
                )
                .with(TemporalAdjusters.previousOrSame(MONDAY))
                .minusWeeks(1);

        LocalDate dataFim = dataInicio.plusDays(6);

        // Intervalo utilizado para consultar o DynamoDB.
        Instant inicio = dataInicio
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        Instant fimExclusivo = dataInicio
                .plusWeeks(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        // Recupera as avaliações do período.
        List<Avaliacao> avaliacoes =
                avaliacaoRepository
                        .listarPorPeriodo(inicio, fimExclusivo)
                        .stream()
                        .filter(avaliacao ->
                                avaliacao.getDataEnvio() != null
                                        && !avaliacao.getDataEnvio()
                                        .isBefore(inicio)
                                        && avaliacao.getDataEnvio()
                                        .isBefore(fimExclusivo)
                        )
                        .toList();

        // Monta o relatório com as estatísticas.
        RelatorioSemanal relatorio = new RelatorioSemanal(
                dataInicio,
                dataFim,
                mediaService.calcular(avaliacoes),
                avaliacoes.size(),
                avaliacoes,
                diaService.calcular(avaliacoes),
                urgenciaService.calcular(avaliacoes)
        );

        // Converte o relatório para DTO.
        RelatorioSemanalDto dto =
                relatorioMapper.toDto(relatorio);

        // Encaminha o relatório para envio.
        relatorioService.enviarRelatorio(dto);

        return dto;
    }
}
