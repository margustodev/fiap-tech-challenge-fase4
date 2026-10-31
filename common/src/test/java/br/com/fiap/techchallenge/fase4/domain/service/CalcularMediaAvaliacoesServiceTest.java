
package br.com.fiap.techchallenge.fase4.domain.service;

import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalcularMediaAvaliacoesServiceTest {

    private final CalcularMediaAvaliacoesService service =
            new CalcularMediaAvaliacoesService();

    @Test
    void deveCalcularMediaDasAvaliacoes() {
        List<Avaliacao> avaliacoes = List.of(
                criarAvaliacao(8),
                criarAvaliacao(6),
                criarAvaliacao(10),
                criarAvaliacao(4)
        );

        Double resultado = service.calcular(avaliacoes);

        assertEquals(7.0, resultado, 0.0001);
    }

    @Test
    void deveCalcularMediaDecimal() {
        List<Avaliacao> avaliacoes = List.of(
                criarAvaliacao(8),
                criarAvaliacao(9)
        );

        Double resultado = service.calcular(avaliacoes);

        assertEquals(8.5, resultado, 0.0001);
    }

    @Test
    void deveRetornarZeroParaListaVazia() {
        Double resultado = service.calcular(List.of());

        assertEquals(0.0, resultado, 0.0001);
    }

    @Test
    void deveRetornarZeroParaListaNula() {
        Double resultado = service.calcular(null);

        assertEquals(0.0, resultado, 0.0001);
    }

    private Avaliacao criarAvaliacao(Integer nota) {
        return new Avaliacao(
                null,
                "Avaliação de teste",
                nota,
                null,
                null
        );
    }
}
