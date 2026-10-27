
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

/**
 * Testes unitarios da Lambda de notificacoes.
 */
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

        assertEquals(
                "Curso desatualizado",
                notificacao.getDescricao()
        );

        assertEquals(
                Urgencia.CRITICA,
                notificacao.getUrgencia()
        );

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

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handleRequest(evento, null)
        );

        assertEquals(
                "Registro do DynamoDB inválido.",
                erro.getMessage()
        );

        assertEquals(0, notificacoes.size());
    }

    @Test
    void naoDeveNotificarQuandoUrgenciaForMedia() {

        DynamodbEvent evento = criarEvento("MEDIA", "INSERT");

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
    void naoDeveNotificarQuandoEventoForRemove() {

        DynamodbEvent evento = criarEvento("CRITICA", "REMOVE");

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
    void deveProcessarMultiplosRegistrosCorretamente() {

        // Cria quatro registros diferentes
        DynamodbEvent.DynamodbStreamRecord registroBaixo =
                criarEvento("BAIXA", "INSERT").getRecords().get(0);

        DynamodbEvent.DynamodbStreamRecord registroAlto =
                criarEvento("ALTA", "INSERT").getRecords().get(0);

        DynamodbEvent.DynamodbStreamRecord registroCritico =
                criarEvento("CRITICA", "INSERT").getRecords().get(0);

        DynamodbEvent.DynamodbStreamRecord registroModify =
                criarEvento("CRITICA", "MODIFY").getRecords().get(0);

        // Simula um lote recebido pela Lambda
        DynamodbEvent evento = new DynamodbEvent();

        evento.setRecords(List.of(
                registroBaixo,
                registroAlto,
                registroCritico,
                registroModify
        ));

        List<NotificacaoUrgenciaDto> notificacoes = new ArrayList<>();
        NotificacaoService servicoSimulado = notificacoes::add;

        CriticalNotificationHandler handler =
                new CriticalNotificationHandler(
                        new DynamoDbStreamMapper(),
                        servicoSimulado
                );

        handler.handleRequest(evento, null);

        // Apenas ALTA e CRITICA devem gerar notificacao
        assertEquals(2, notificacoes.size());

        assertEquals(
                Urgencia.ALTA,
                notificacoes.get(0).getUrgencia()
        );

        assertEquals(
                Urgencia.CRITICA,
                notificacoes.get(1).getUrgencia()
        );
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
