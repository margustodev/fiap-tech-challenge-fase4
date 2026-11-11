package br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompositeNotificacaoServiceTest {

    @Test
    void deveEncaminharParaTodosOsCanais() {
        List<NotificacaoUrgenciaDto> ses = new ArrayList<>();
        List<NotificacaoUrgenciaDto> sns = new ArrayList<>();

        NotificacaoService composite = new CompositeNotificacaoService(
                ses::add,
                sns::add
        );

        NotificacaoUrgenciaDto notificacao = new NotificacaoUrgenciaDto(
                "Feedback critico",
                Urgencia.CRITICA,
                Instant.parse("2026-10-19T18:00:00Z")
        );

        composite.notificarUrgencia(notificacao);

        assertEquals(1, ses.size());
        assertEquals(1, sns.size());
        assertEquals(notificacao, ses.getFirst());
        assertEquals(notificacao, sns.getFirst());
    }

    @Test
    void deveExigirAoMenosUmCanal() {
        assertThrows(
                IllegalArgumentException.class,
                CompositeNotificacaoService::new
        );
    }
}
