
package br.com.fiap.techchallenge.fase4.domain.service;

import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;

import java.util.List;

/**
 * Serviço de domínio responsável por calcular
 * a média das notas das avaliações.
 */
public class CalcularMediaAvaliacoesService {

    public Double calcular(List<Avaliacao> avaliacoes) {

        if (avaliacoes == null || avaliacoes.isEmpty()) {
            return 0.0;
        }

        return avaliacoes.stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);
    }
}
