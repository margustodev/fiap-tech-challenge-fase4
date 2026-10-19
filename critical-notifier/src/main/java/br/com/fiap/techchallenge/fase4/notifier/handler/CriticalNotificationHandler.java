package br.com.fiap.techchallenge.fase4.notifier.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

/**
 * Handler Lambda: processa eventos do DynamoDB Streams.
 * Responsabilidade única: detectar avaliações críticas e acionar notificação.
 */
public class CriticalNotificationHandler implements RequestHandler<DynamodbEvent, Void> {

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        // TODO: percorrer records INSERT, filtrar urgência crítica/alta e notificar
        return null;
    }

}
