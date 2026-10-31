
package br.com.fiap.techchallenge.fase4.domain.service;

import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorDia;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalcularQuantidadePorDiaServiceTest {

    private final CalcularQuantidadePorDiaService service =
            new CalcularQuantidadePorDiaService();

    @Test
    void deveContabilizarAvaliacoesPorDia() {
        List<Avaliacao> avaliacoes = List.of(
                criarAvaliacao("2026-10-26T10:00:00Z"),
                criarAvaliacao("2026-10-26T15:00:00Z"),
                criarAvaliacao("2026-10-27T09:00:00Z"),
                criarAvaliacao("2026-10-28T12:00:00Z"),
                criarAvaliacao("2026-10-28T18:00:00Z")
        );

        List<EstatisticaPorDia> resultado =
                service.calcular(avaliacoes);

        assertEquals(3, resultado.size());

        assertEquals(
                LocalDate.of(2026, 10, 26),
                resultado.get(0).getData()
        );
        assertEquals(2L, resultado.get(0).getQuantidade());

        assertEquals(
                LocalDate.of(2026, 10, 27),
                resultado.get(1).getData()
        );
        assertEquals(1L, resultado.get(1).getQuantidade());

        assertEquals(
                LocalDate.of(2026, 10, 28),
                resultado.get(2).getData()
        );
        assertEquals(2L, resultado.get(2).getQuantidade());
    }

    @Test
    void deveOrdenarAsDatasCronologicamente() {
        List<Avaliacao> avaliacoes = List.of(
                criarAvaliacao("2026-10-28T12:00:00Z"),
                criarAvaliacao("2026-10-26T10:00:00Z"),
                criarAvaliacao("2026-10-27T09:00:00Z")
        );

        List<EstatisticaPorDia> resultado =
                service.calcular(avaliacoes);

        assertEquals(
                LocalDate.of(2026, 10, 26),
                resultado.get(0).getData()
        );
        assertEquals(
                LocalDate.of(2026, 10, 27),
                resultado.get(1).getData()
        );
        assertEquals(
                LocalDate.of(2026, 10, 28),
                resultado.get(2).getData()
        );
    }

    @Test
    void deveConsiderarODiaEmUTC() {
        List<Avaliacao> avaliacoes = List.of(
                criarAvaliacao("2026-10-26T23:59:59Z"),
                criarAvaliacao("2026-10-27T00:00:00Z")
        );

        List<EstatisticaPorDia> resultado =
                service.calcular(avaliacoes);

        assertEquals(2, resultado.size());
        assertEquals(
                LocalDate.of(2026, 10, 26),
                resultado.get(0).getData()
        );
        assertEquals(
                LocalDate.of(2026, 10, 27),
                resultado.get(1).getData()
        );
    }

    @Test
    void deveRetornarListaVaziaSemAvaliacoes() {
        assertTrue(service.calcular(List.of()).isEmpty());
        assertTrue(service.calcular(null).isEmpty());
    }

    private Avaliacao criarAvaliacao(String dataEnvio) {
        return new Avaliacao(
                null,
                "Avaliação de teste",
                8,
                null,
                Instant.parse(dataEnvio)
        );
    }
}
