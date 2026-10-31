
package br.com.fiap.techchallenge.fase4.application.dto;

import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorDia;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorUrgencia;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO responsável por disponibilizar os dados
 * do relatório semanal de avaliações.
 */
public class RelatorioSemanalDto {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Double mediaAvaliacoes;
    private Integer totalAvaliacoes;
    private List<AvaliacaoResumoDto> avaliacoes;
    private List<EstatisticaPorDia> quantidadePorDia;
    private List<EstatisticaPorUrgencia> quantidadePorUrgencia;

    public RelatorioSemanalDto() {
    }

    public RelatorioSemanalDto(
            LocalDate dataInicio,
            LocalDate dataFim,
            Double mediaAvaliacoes,
            Integer totalAvaliacoes,
            List<AvaliacaoResumoDto> avaliacoes,
            List<EstatisticaPorDia> quantidadePorDia,
            List<EstatisticaPorUrgencia> quantidadePorUrgencia) {

        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.mediaAvaliacoes = mediaAvaliacoes;
        this.totalAvaliacoes = totalAvaliacoes;
        this.avaliacoes = avaliacoes;
        this.quantidadePorDia = quantidadePorDia;
        this.quantidadePorUrgencia = quantidadePorUrgencia;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public Double getMediaAvaliacoes() {
        return mediaAvaliacoes;
    }

    public void setMediaAvaliacoes(Double mediaAvaliacoes) {
        this.mediaAvaliacoes = mediaAvaliacoes;
    }

    public Integer getTotalAvaliacoes() {
        return totalAvaliacoes;
    }

    public void setTotalAvaliacoes(Integer totalAvaliacoes) {
        this.totalAvaliacoes = totalAvaliacoes;
    }

    public List<AvaliacaoResumoDto> getAvaliacoes() {
        return avaliacoes;
    }

    public void setAvaliacoes(List<AvaliacaoResumoDto> avaliacoes) {
        this.avaliacoes = avaliacoes;
    }

    public List<EstatisticaPorDia> getQuantidadePorDia() {
        return quantidadePorDia;
    }

    public void setQuantidadePorDia(
            List<EstatisticaPorDia> quantidadePorDia) {
        this.quantidadePorDia = quantidadePorDia;
    }

    public List<EstatisticaPorUrgencia> getQuantidadePorUrgencia() {
        return quantidadePorUrgencia;
    }

    public void setQuantidadePorUrgencia(
            List<EstatisticaPorUrgencia> quantidadePorUrgencia) {
        this.quantidadePorUrgencia = quantidadePorUrgencia;
    }
}
