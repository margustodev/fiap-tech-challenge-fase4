
package br.com.fiap.techchallenge.fase4.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Representa o relatório semanal de avaliações.
 *
 * Contém o período analisado, a média das notas,
 * o total de avaliações e as estatísticas
 * agrupadas por dia e por urgência.
 */
public class RelatorioSemanal {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Double mediaAvaliacoes;
    private Integer totalAvaliacoes;
    private List<Avaliacao> avaliacoes;
    private List<EstatisticaPorDia> quantidadePorDia;
    private List<EstatisticaPorUrgencia> quantidadePorUrgencia;

    public RelatorioSemanal() {
    }

    public RelatorioSemanal(
            LocalDate dataInicio,
            LocalDate dataFim,
            Double mediaAvaliacoes,
            Integer totalAvaliacoes,
            List<Avaliacao> avaliacoes,
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

    public List<Avaliacao> getAvaliacoes() {
        return avaliacoes;
    }

    public void setAvaliacoes(List<Avaliacao> avaliacoes) {
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
