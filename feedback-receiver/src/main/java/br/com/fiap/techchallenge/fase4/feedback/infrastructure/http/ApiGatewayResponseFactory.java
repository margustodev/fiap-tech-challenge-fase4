package br.com.fiap.techchallenge.fase4.feedback.infrastructure.http;

import br.com.fiap.techchallenge.fase4.application.dto.ErroResponse;
import br.com.fiap.techchallenge.fase4.infrastructure.util.JsonMapper;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Fábrica de respostas HTTP padronizadas para o API Gateway.
 */
public class ApiGatewayResponseFactory {

    private final JsonMapper jsonMapper;

    public ApiGatewayResponseFactory(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public APIGatewayProxyResponseEvent sucesso(int statusCode, Object body) {
        return montar(statusCode, jsonMapper.toJson(body));
    }

    public APIGatewayProxyResponseEvent erro(int statusCode, String mensagem) {
        return montar(statusCode, jsonMapper.toJson(new ErroResponse(mensagem)));
    }

    private APIGatewayProxyResponseEvent montar(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(body);
    }

}
