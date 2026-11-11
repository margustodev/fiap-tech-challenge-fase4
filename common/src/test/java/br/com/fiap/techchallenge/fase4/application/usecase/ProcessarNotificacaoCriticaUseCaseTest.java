package br.com.fiap.techchallenge.fase4.application.usecase;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessarNotificacaoCriticaUseCaseTest {

    @Test
    void deveNotificarQuandoUrgenciaCritica() {
        List<NotificacaoUrgenciaDto> enviadas = new ArrayList<>();
        NotificacaoService servico = enviadas::add;
        ProcessarNotificacaoCriticaUseCase useCase =
                new ProcessarNotificacaoCriticaUseCase(servico);

        boolean notificado = useCase.executar(avaliacao(Urgencia.CRITICA));

        assertTrue(notificado);
        assertEquals(1, enviadas.size());
        assertEquals(Urgencia.CRITICA, enviadas.getFirst().getUrgencia());
    }

    @Test
    void deveNotificarQuandoUrgenciaAlta() {
        List<NotificacaoUrgenciaDto> enviadas = new ArrayList<>();
        ProcessarNotificacaoCriticaUseCase useCase =
                new ProcessarNotificacaoCriticaUseCase(enviadas::add);

        assertTrue(useCase.executar(avaliacao(Urgencia.ALTA)));
        assertEquals(1, enviadas.size());
    }

    @Test
    void naoDeveNotificarQuandoUrgenciaBaixaOuMedia() {
        List<NotificacaoUrgenciaDto> enviadas = new ArrayList<>();
        ProcessarNotificacaoCriticaUseCase useCase =
                new ProcessarNotificacaoCriticaUseCase(enviadas::add);

        assertFalse(useCase.executar(avaliacao(Urgencia.BAIXA)));
        assertFalse(useCase.executar(avaliacao(Urgencia.MEDIA)));
        assertTrue(enviadas.isEmpty());
    }

    private Avaliacao avaliacao(Urgencia urgencia) {
        return new Avaliacao(
                "id-1",
                "Curso desatualizado",
                2,
                urgencia,
                Instant.parse("2026-10-19T18:00:00Z")
        );
    }
}
