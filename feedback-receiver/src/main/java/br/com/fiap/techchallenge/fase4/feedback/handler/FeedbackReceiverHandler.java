package br.com.fiap.techchallenge.fase4.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Handler Lambda: recebe POST /avaliacao via API Gateway.
 * Responsabilidade única: entrada HTTP e orquestração do caso de uso de recebimento.
 */
public class FeedbackReceiverHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        // TODO: deserializar body, validar, executar ReceberAvaliacaoUseCase e retornar response
        return null;
    }

}
