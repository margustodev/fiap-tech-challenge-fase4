
package br.com.fiap.techchallenge.fase4.application.mapper;

import br.com.fiap.techchallenge.fase4.application.dto.RelatorioSemanalDto;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorDia;
import br.com.fiap.techchallenge.fase4.domain.model.EstatisticaPorUrgencia;
import br.com.fiap.techchallenge.fase4.domain.model.RelatorioSemanal;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RelatorioMapperTest {

    private final RelatorioMapper mapper =
            new RelatorioMapper();

    @Test
    void deveConverterRelatorioSemanalParaDto() {

        LocalDate inicio = LocalDate.of(2026, 10, 19);
        LocalDate fim = LocalDate.of(2026, 10, 25);

        Avaliacao avaliacao = new Avaliacao(
                "avaliacao-001",
                "Curso precisa melhorar",
                2,
                Urgencia.CRITICA,
                Instant.parse("2026-10-20T10:00:00Z")
        );

        RelatorioSemanal relatorio = new RelatorioSemanal(
                inicio,
                fim,
                2.0,
                1,
                List.of(avaliacao),
                List.of(new EstatisticaPorDia(
                        LocalDate.of(2026, 10, 20),
                        1L
                )),
                List.of(new EstatisticaPorUrgencia(
                        Urgencia.CRITICA,
                        1L
                ))
        );

        RelatorioSemanalDto resultado =
                mapper.toDto(relatorio);

        assertNotNull(resultado);
        assertEquals(inicio, resultado.getDataInicio());
        assertEquals(fim, resultado.getDataFim());
        assertEquals(2.0, resultado.getMediaAvaliacoes(), 0.0001);
        assertEquals(1, resultado.getTotalAvaliacoes());

        assertEquals(1, resultado.getAvaliacoes().size());
        assertEquals(
                "Curso precisa melhorar",
                resultado.getAvaliacoes().get(0).getDescricao()
        );
        assertEquals(
                Urgencia.CRITICA,
                resultado.getAvaliacoes().get(0).getUrgencia()
        );
        assertEquals(
                Instant.parse("2026-10-20T10:00:00Z"),
                resultado.getAvaliacoes().get(0).getDataEnvio()
        );

        assertEquals(1, resultado.getQuantidadePorDia().size());
        assertEquals(
                1L,
                resultado.getQuantidadePorDia().get(0).getQuantidade()
        );

        assertEquals(1, resultado.getQuantidadePorUrgencia().size());
        assertEquals(
                Urgencia.CRITICA,
                resultado.getQuantidadePorUrgencia().get(0).getUrgencia()
        );
    }

    @Test
    void deveRetornarListasVaziasQuandoNaoExistiremDados() {

        RelatorioSemanal relatorio = new RelatorioSemanal(
                LocalDate.of(2026, 10, 19),
                LocalDate.of(2026, 10, 25),
                0.0,
                0,
                null,
                null,
                null
        );

        RelatorioSemanalDto resultado =
                mapper.toDto(relatorio);

        assertNotNull(resultado);
        assertTrue(resultado.getAvaliacoes().isEmpty());
        assertTrue(resultado.getQuantidadePorDia().isEmpty());
        assertTrue(resultado.getQuantidadePorUrgencia().isEmpty());
    }

    @Test
    void deveRejeitarRelatorioNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toDto(null)
        );
    }
}
