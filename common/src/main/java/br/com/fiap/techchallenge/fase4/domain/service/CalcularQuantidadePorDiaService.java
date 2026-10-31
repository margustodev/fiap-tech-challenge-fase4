
package br.com.fiap.techchallenge.fase4.domain.service;

import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorDia;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Serviço responsável por contabilizar a quantidade
 * de avaliações recebidas em cada dia.
 */
public class CalcularQuantidadePorDiaService {

    public List<EstatisticaPorDia> calcular(
            List<Avaliacao> avaliacoes) {

        if (avaliacoes == null || avaliacoes.isEmpty()) {
            return List.of();
        }

        Map<LocalDate, Long> quantidadePorDia =
                avaliacoes.stream()
                        .collect(Collectors.groupingBy(
                                avaliacao -> avaliacao
                                        .getDataEnvio()
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate(),
                                TreeMap::new,
                                Collectors.counting()
                        ));

        return quantidadePorDia.entrySet()
                .stream()
                .map(entry -> new EstatisticaPorDia(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }
}
