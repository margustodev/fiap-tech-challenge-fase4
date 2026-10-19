package br.com.fiap.techchallenge.fase4.report.infrastructure.aggregator;

import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorDia;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorUrgencia;
import br.com.fiap.techchallenge.fase4.domain.model.RelatorioSemanal;

import java.util.List;

/**
 * Agrega avaliações da semana em estatísticas do relatório.
 */
public class RelatorioAggregator {

    public RelatorioSemanal agregar(List<Avaliacao> avaliacoes) {
        // TODO: calcular média, quantidade por dia e por urgência
        return null;
    }

    public List<EstatisticaPorDia> agruparPorDia(List<Avaliacao> avaliacoes) {
        // TODO: agrupar quantidade de avaliações por dia
        return List.of();
    }

    public List<EstatisticaPorUrgencia> agruparPorUrgencia(List<Avaliacao> avaliacoes) {
        // TODO: agrupar quantidade de avaliações por urgência
        return List.of();
    }

}
