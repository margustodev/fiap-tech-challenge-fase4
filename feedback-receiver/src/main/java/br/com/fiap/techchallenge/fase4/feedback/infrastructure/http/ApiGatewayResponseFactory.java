package br.com.fiap.techchallenge.fase4.feedback.infrastructure.http;

import br.com.fiap.techchallenge.fase4.infrastructure.util.JsonMapper;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

/**
 * Fábrica de respostas HTTP padronizadas para o API Gateway.
 */
public class ApiGatewayResponseFactory {
    private final JsonMapper jsonMapper = new JsonMapper();

    public APIGatewayProxyResponseEvent sucesso(int statusCode, Object body) {
        // TODO: serializar body e montar response com headers
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody(jsonMapper.toJson(body));    }

    public APIGatewayProxyResponseEvent erro(int statusCode, String mensagem) {
        // TODO: montar response de erro
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody(jsonMapper.toJson(Map.of("erro", mensagem)));
    }

}
