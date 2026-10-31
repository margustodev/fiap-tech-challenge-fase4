
package br.com.fiap.techchallenge.fase4.domain.service;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorUrgencia;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço responsável por contabilizar avaliações
 * agrupadas por nível de urgência.
 */
public class CalcularQuantidadePorUrgenciaService {

    public List<EstatisticaPorUrgencia> calcular(
            List<Avaliacao> avaliacoes) {

        if (avaliacoes == null || avaliacoes.isEmpty()) {
            return List.of();
        }

        Map<Urgencia, Long> quantidadePorUrgencia =
                avaliacoes.stream()
                        .collect(Collectors.groupingBy(
                                Avaliacao::getUrgencia,
                                () -> new EnumMap<>(Urgencia.class),
                                Collectors.counting()
                        ));

        return Arrays.stream(Urgencia.values())
                .map(urgencia -> new EstatisticaPorUrgencia(
                        urgencia,
                        quantidadePorUrgencia.getOrDefault(
                                urgencia, 0L
                        )
                ))
                .toList();
    }
}
