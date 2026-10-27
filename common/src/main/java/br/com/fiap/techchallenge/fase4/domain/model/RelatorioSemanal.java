package br.com.fiap.techchallenge.fase4.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Agregado com os dados do relatório semanal de feedbacks.
 */
public class RelatorioSemanal {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Double mediaAvaliacoes;
    private Integer totalAvaliacoes;
    private List<Avaliacao> avaliacoes;
    private List<EstatisticaPorDia> quantidadePorDia;
    private List<EstatisticaPorUrgencia> quantidadePorUrgencia;

    // Getters
    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public Double getMediaAvaliacoes() {
        return mediaAvaliacoes;
    }

    public Integer getTotalAvaliacoes() {
        return totalAvaliacoes;
    }

    public List<Avaliacao> getAvaliacoes() {
        return avaliacoes;
    }

    public List<EstatisticaPorDia> getQuantidadePorDia() {
        return quantidadePorDia;
    }

    public List<EstatisticaPorUrgencia> getQuantidadePorUrgencia() {
        return quantidadePorUrgencia;
    }


}
