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

}
