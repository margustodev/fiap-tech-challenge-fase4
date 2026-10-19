package br.com.fiap.techchallenge.fase4.notifier.infrastructure.mapper;

import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

/**
 * Converte records do DynamoDB Streams em entidades de domínio.
 */
public class DynamoDbStreamMapper {

    public Avaliacao toAvaliacao(DynamodbEvent.DynamodbStreamRecord record) {
        // TODO: mapear NewImage do stream para Avaliacao
        return null;
    }

}
