
package br.com.fiap.techchallenge.fase4.notifier.infrastructure.mapper;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import java.time.Instant;
import java.util.Map;

/**
 * Converte registros do DynamoDB Streams em objetos Avaliacao.
 */
public class DynamoDbStreamMapper {

    public Avaliacao toAvaliacao(DynamodbEvent.DynamodbStreamRecord record) {

        if (record == null || record.getDynamodb() == null
                || record.getDynamodb().getNewImage() == null) {
            throw new IllegalArgumentException("Registro do DynamoDB inválido.");
        }

        Map<String, AttributeValue> dados = record.getDynamodb().getNewImage();

        String id = obterTexto(dados, "id");
        String descricao = obterTexto(dados, "descricao");
        Integer nota = obterNumero(dados, "nota");

        Urgencia urgencia = Urgencia.valueOf(
                obterTexto(dados, "urgencia")
        );

        Instant dataEnvio = Instant.parse(
                obterTexto(dados, "dataEnvio")
        );

        return new Avaliacao(
                id,
                descricao,
                nota,
                urgencia,
                dataEnvio
        );
    }

    private String obterTexto(Map<String, AttributeValue> dados, String campo) {

        AttributeValue valor = dados.get(campo);

        if (valor == null || valor.getS() == null) {
            throw new IllegalArgumentException(
                    "Campo obrigatório ausente: " + campo
            );
        }

        return valor.getS();
    }

    private Integer obterNumero(Map<String, AttributeValue> dados, String campo) {

        AttributeValue valor = dados.get(campo);

        if (valor == null || valor.getN() == null) {
            throw new IllegalArgumentException(
                    "Campo numérico obrigatório ausente: " + campo
            );
        }

        return Integer.valueOf(valor.getN());
    }
}
