package br.com.fiap.techchallenge.fase4.application.dto;

import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorDia;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorUrgencia;

import java.time.LocalDate;
import java.util.List;

/**
 * Dados do relatório semanal de feedbacks enviado aos administradores.
 */
public class RelatorioSemanalDto {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Double mediaAvaliacoes;
    private Integer totalAvaliacoes;
    private List<AvaliacaoResumoDto> avaliacoes;
    private List<EstatisticaPorDia> quantidadePorDia;
    private List<EstatisticaPorUrgencia> quantidadePorUrgencia;

    public RelatorioSemanalDto(LocalDate dataInicio, LocalDate dataFim, Double mediaAvaliacoes, Integer totalAvaliacoes, List<AvaliacaoResumoDto> avaliacoes, List<EstatisticaPorDia> quantidadePorDia, List<EstatisticaPorUrgencia> quantidadePorUrgencia) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.mediaAvaliacoes = mediaAvaliacoes;
        this.totalAvaliacoes = totalAvaliacoes;
        this.avaliacoes = avaliacoes;
        this.quantidadePorDia = quantidadePorDia;
        this.quantidadePorUrgencia = quantidadePorUrgencia;
    }
}
