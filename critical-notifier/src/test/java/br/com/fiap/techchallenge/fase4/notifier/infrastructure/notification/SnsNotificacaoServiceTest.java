package br.com.fiap.techchallenge.fase4.notifier.infrastructure.notification;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.exception.NotificacaoException;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnsNotificacaoServiceTest {

    @Test
    void devePublicarMensagemNoTopicoSns() {
        AtomicReference<String> topic = new AtomicReference<>();
        AtomicReference<String> subject = new AtomicReference<>();
        AtomicReference<String> message = new AtomicReference<>();

        SnsNotificacaoService servico = new SnsNotificacaoService(
                "arn:aws:sns:us-east-1:123:alerts",
                (topicArn, assunto, corpo) -> {
                    topic.set(topicArn);
                    subject.set(assunto);
                    message.set(corpo);
                    return "msg-001";
                }
        );

        servico.notificarUrgencia(new NotificacaoUrgenciaDto(
                "Curso com conteudo desatualizado",
                Urgencia.CRITICA,
                Instant.parse("2026-10-19T18:00:00Z")
        ));

        assertEquals("arn:aws:sns:us-east-1:123:alerts", topic.get());
        assertEquals("Alerta de avaliacao - Urgencia CRITICA", subject.get());
        assertTrue(message.get().contains("Curso com conteudo desatualizado"));
        assertTrue(message.get().contains("CRITICA"));
        assertTrue(message.get().contains("2026-10-19T18:00:00Z"));
    }

    @Test
    void deveFalharQuandoTopicoNaoEstiverConfigurado() {
        assertThrows(
                IllegalStateException.class,
                () -> new SnsNotificacaoService("  ", (a, b, c) -> "id")
        );
    }

    @Test
    void deveEncapsularFalhaDePublicacao() {
        SnsNotificacaoService servico = new SnsNotificacaoService(
                "arn:aws:sns:us-east-1:123:alerts",
                (topicArn, subject, message) -> {
                    throw new IllegalStateException("Falha SNS");
                }
        );

        NotificacaoException erro = assertThrows(
                NotificacaoException.class,
                () -> servico.notificarUrgencia(new NotificacaoUrgenciaDto(
                        "Problema",
                        Urgencia.ALTA,
                        Instant.parse("2026-10-19T18:00:00Z")
                ))
        );

        assertTrue(erro.getMessage().contains("SNS"));
    }
}
