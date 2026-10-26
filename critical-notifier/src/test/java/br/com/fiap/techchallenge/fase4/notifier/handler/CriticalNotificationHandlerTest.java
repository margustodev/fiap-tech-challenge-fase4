
package br.com.fiap.techchallenge.fase4.notifier.handler;

import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.application.port.out.NotificacaoService;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.notifier.infrastructure.mapper.DynamoDbStreamMapper;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CriticalNotificationHandlerTest {

    @Test
    void deveNotificarQuandoAvaliacaoForCritica() {

        DynamodbEvent evento = criarEvento("CRITICA", "INSERT");

        List<NotificacaoUrgenciaDto> notificacoes = new ArrayList<>();
        NotificacaoService servicoSimulado = notificacoes::add;

        CriticalNotificationHandler handler =
                new CriticalNotificationHandler(
                        new DynamoDbStreamMapper(),
                        servicoSimulado
                );

        handler.handleRequest(evento, null);

        assertEquals(1, notificacoes.size());

        NotificacaoUrgenciaDto notificacao = notificacoes.get(0);

        assertEquals("Curso desatualizado", notificacao.getDescricao());
        assertEquals(Urgencia.CRITICA, notificacao.getUrgencia());
        assertEquals(
                Instant.parse("2026-10-19T18:00:00Z"),
                notificacao.getDataEnvio()
        );
    }

    @Test
    void naoDeveNotificarQuandoUrgenciaForBaixa() {

        DynamodbEvent evento = criarEvento("BAIXA", "INSERT");

        List<NotificacaoUrgenciaDto> notificacoes = new ArrayList<>();
        NotificacaoService servicoSimulado = notificacoes::add;

        CriticalNotificationHandler handler =
                new CriticalNotificationHandler(
                        new DynamoDbStreamMapper(),
                        servicoSimulado
                );

        handler.handleRequest(evento, null);

        assertEquals(0, notificacoes.size());
    }

    @Test
    void deveNotificarQuandoUrgenciaForAlta() {

        DynamodbEvent evento = criarEvento("ALTA", "INSERT");

        List<NotificacaoUrgenciaDto> notificacoes = new ArrayList<>();
        NotificacaoService servicoSimulado = notificacoes::add;

        CriticalNotificationHandler handler =
                new CriticalNotificationHandler(
                        new DynamoDbStreamMapper(),
                        servicoSimulado
                );

        handler.handleRequest(evento, null);

        assertEquals(1, notificacoes.size());
        assertEquals(
                Urgencia.ALTA,
                notificacoes.get(0).getUrgencia()
        );
    }

    @Test
    void naoDeveNotificarQuandoEventoForModify() {

        DynamodbEvent evento = criarEvento("CRITICA", "MODIFY");

        List<NotificacaoUrgenciaDto> notificacoes = new ArrayList<>();
        NotificacaoService servicoSimulado = notificacoes::add;

        CriticalNotificationHandler handler =
                new CriticalNotificationHandler(
                        new DynamoDbStreamMapper(),
                        servicoSimulado
                );

        handler.handleRequest(evento, null);

        assertEquals(0, notificacoes.size());
    }

    @Test
    void deveRejeitarEventoSemDadosDaAvaliacao() {

        // Simula um evento INSERT sem NewImage
        StreamRecord streamRecord = new StreamRecord();

        DynamodbEvent.DynamodbStreamRecord record =
                new DynamodbEvent.DynamodbStreamRecord();

        record.setEventName("INSERT");
        record.setDynamodb(streamRecord);

        DynamodbEvent evento = new DynamodbEvent();
        evento.setRecords(List.of(record));

        List<NotificacaoUrgenciaDto> notificacoes = new ArrayList<>();
        NotificacaoService servicoSimulado = notificacoes::add;

        CriticalNotificationHandler handler =
                new CriticalNotificationHandler(
                        new DynamoDbStreamMapper(),
                        servicoSimulado
                );

        // Verifica se o evento invalido e rejeitado
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handleRequest(evento, null)
        );

        assertEquals(
                "Registro do DynamoDB inválido.",
                erro.getMessage()
        );

        // Nenhuma notificacao deve ser acionada
        assertEquals(0, notificacoes.size());
    }

    /**
     * Cria um evento simulado do DynamoDB Streams.
     */
    private DynamodbEvent criarEvento(
            String urgencia,
            String tipoEvento) {

        Map<String, AttributeValue> dados = Map.of(
                "id", new AttributeValue().withS("avaliacao-001"),
                "descricao", new AttributeValue().withS("Curso desatualizado"),
                "nota", new AttributeValue().withN("2"),
                "urgencia", new AttributeValue().withS(urgencia),
                "dataEnvio", new AttributeValue()
                        .withS("2026-10-19T18:00:00Z")
        );

        StreamRecord streamRecord = new StreamRecord();
        streamRecord.setNewImage(dados);

        DynamodbEvent.DynamodbStreamRecord record =
                new DynamodbEvent.DynamodbStreamRecord();

        record.setEventName(tipoEvento);
        record.setDynamodb(streamRecord);

        DynamodbEvent evento = new DynamodbEvent();
        evento.setRecords(List.of(record));

        return evento;
    }
}
