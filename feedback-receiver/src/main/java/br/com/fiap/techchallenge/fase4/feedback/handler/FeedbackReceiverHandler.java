package br.com.fiap.techchallenge.fase4.feedback.handler;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoRequest;
import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResponse;
import br.com.fiap.techchallenge.fase4.application.exception.AvaliacaoInvalidaException;
import br.com.fiap.techchallenge.fase4.application.usecase.ReceberAvaliacaoUseCase;
import br.com.fiap.techchallenge.fase4.feedback.config.FeedbackReceiverConfig;
import br.com.fiap.techchallenge.fase4.feedback.infrastructure.http.ApiGatewayResponseFactory;
import br.com.fiap.techchallenge.fase4.infrastructure.util.JsonMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler Lambda: recebe POST /avaliacao via API Gateway.
 * Responsabilidade única: entrada HTTP e orquestração do caso de uso de recebimento.
 */
public class FeedbackReceiverHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger =
            LoggerFactory.getLogger(FeedbackReceiverHandler.class);

    private final JsonMapper jsonMapper = new JsonMapper();
    private final ApiGatewayResponseFactory responseFactory =
            new ApiGatewayResponseFactory();
    private final ReceberAvaliacaoUseCase receberAvaliacaoUseCase =
            new FeedbackReceiverConfig().receberAvaliacaoUseCase();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        // TODO: deserializar body, validar, executar ReceberAvaliacaoUseCase e retornar response
        try {
            if (input == null || input.getBody() == null || input.getBody().isBlank()) {
                throw new AvaliacaoInvalidaException(
                        "O corpo da requisição é obrigatório."
                );
            }

            AvaliacaoRequest request =
                    jsonMapper.fromJson(input.getBody(), AvaliacaoRequest.class);

            AvaliacaoResponse response =
                    receberAvaliacaoUseCase.executar(request);

            logger.info("Avaliação registrada. id={}", response.getId());

            return responseFactory.sucesso(201, response);

        } catch (AvaliacaoInvalidaException e) {
            logger.warn("Dados da avaliação inválidos: {}", e.getMessage());
            return responseFactory.erro(400, e.getMessage());

        } catch (IllegalArgumentException e) {
            logger.warn("Falha ao interpretar o JSON da requisição.", e);
            return responseFactory.erro(400, "JSON inválido.");

        } catch (Exception e) {
            logger.error("Falha ao processar avaliação.", e);
            return responseFactory.erro(
                    500,
                    "Erro interno ao processar a avaliação."
            );
        }
    }

}
