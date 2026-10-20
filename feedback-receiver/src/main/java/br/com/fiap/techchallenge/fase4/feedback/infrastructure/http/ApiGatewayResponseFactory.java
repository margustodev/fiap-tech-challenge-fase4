package br.com.fiap.techchallenge.fase4.feedback.infrastructure.http;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Fábrica de respostas HTTP padronizadas para o API Gateway.
 */
public class ApiGatewayResponseFactory {

    public APIGatewayProxyResponseEvent sucesso(int statusCode, Object body) {
        // TODO: serializar body e montar response com headers
        return null;
    }

    public APIGatewayProxyResponseEvent erro(int statusCode, String mensagem) {
        // TODO: montar response de erro
        return null;
    }

}
