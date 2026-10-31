
package br.com.fiap.techchallenge.fase4.domain.service;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorUrgencia;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalcularQuantidadePorUrgenciaServiceTest {

    private final CalcularQuantidadePorUrgenciaService service =
            new CalcularQuantidadePorUrgenciaService();

    @Test
    void deveContabilizarAvaliacoesPorUrgencia() {
        List<Avaliacao> avaliacoes = List.of(
                criarAvaliacao(Urgencia.CRITICA),
                criarAvaliacao(Urgencia.CRITICA),
                criarAvaliacao(Urgencia.ALTA),
                criarAvaliacao(Urgencia.ALTA),
                criarAvaliacao(Urgencia.ALTA),
                criarAvaliacao(Urgencia.MEDIA),
                criarAvaliacao(Urgencia.MEDIA),
                criarAvaliacao(Urgencia.BAIXA)
        );

        List<EstatisticaPorUrgencia> resultado =
                service.calcular(avaliacoes);

        assertEquals(4, resultado.size());
        assertEquals(2L, quantidadeDe(resultado, Urgencia.CRITICA));
        assertEquals(3L, quantidadeDe(resultado, Urgencia.ALTA));
        assertEquals(2L, quantidadeDe(resultado, Urgencia.MEDIA));
        assertEquals(1L, quantidadeDe(resultado, Urgencia.BAIXA));
    }

    @Test
    void deveRetornarZeroParaUrgenciasSemAvaliacoes() {
        List<Avaliacao> avaliacoes = List.of(
                criarAvaliacao(Urgencia.ALTA)
        );

        List<EstatisticaPorUrgencia> resultado =
                service.calcular(avaliacoes);

        assertEquals(4, resultado.size());
        assertEquals(0L, quantidadeDe(resultado, Urgencia.CRITICA));
        assertEquals(1L, quantidadeDe(resultado, Urgencia.ALTA));
        assertEquals(0L, quantidadeDe(resultado, Urgencia.MEDIA));
        assertEquals(0L, quantidadeDe(resultado, Urgencia.BAIXA));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistemAvaliacoes() {
        assertTrue(service.calcular(List.of()).isEmpty());
    }

    @Test
    void deveRetornarListaVaziaQuandoListaForNula() {
        assertTrue(service.calcular(null).isEmpty());
    }

    private Long quantidadeDe(
            List<EstatisticaPorUrgencia> resultado,
            Urgencia urgencia) {

        return resultado.stream()
                .filter(item -> item.getUrgencia() == urgencia)
                .findFirst()
                .orElseThrow()
                .getQuantidade();
    }

    private Avaliacao criarAvaliacao(Urgencia urgencia) {
        return new Avaliacao(
                null,
                "Avaliação de teste",
                5,
                urgencia,
                null
        );
    }
}
