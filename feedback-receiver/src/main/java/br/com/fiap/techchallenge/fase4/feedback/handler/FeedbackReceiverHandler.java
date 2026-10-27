package br.com.fiap.techchallenge.fase4.feedback.handler;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoRequest;
import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResponse;
import br.com.fiap.techchallenge.fase4.application.exception.AvaliacaoInvalidaException;
import br.com.fiap.techchallenge.fase4.application.port.out.MetricsService;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackReceiverHandler.class);

    private static final String METRIC_SUCESSO = "avaliacao.recebida.sucesso";
    private static final String METRIC_ERRO_VALIDACAO = "avaliacao.recebida.erro_validacao";
    private static final String METRIC_ERRO_INTERNO = "avaliacao.recebida.erro_interno";

    private final FeedbackReceiverConfig config;
    private final JsonMapper jsonMapper;
    private final ApiGatewayResponseFactory responseFactory;
    private final MetricsService metricsService;
    private final ReceberAvaliacaoUseCase useCase;

    public FeedbackReceiverHandler() {
        System.out.println("[FeedbackReceiverHandler] iniciando construtor");
        this.config = new FeedbackReceiverConfig();
        this.jsonMapper = config.jsonMapper();
        this.responseFactory = config.responseFactory();
        this.metricsService = config.metricsService();
        // use case / DynamoDB só na primeira chamada (evita travar o init do runtime)
        this.useCase = null;
        System.out.println("[FeedbackReceiverHandler] construtor ok (DynamoDB lazy)");
    }

    public FeedbackReceiverHandler(
            ReceberAvaliacaoUseCase useCase,
            JsonMapper jsonMapper,
            ApiGatewayResponseFactory responseFactory,
            MetricsService metricsService) {
        this.config = null;
        this.useCase = useCase;
        this.jsonMapper = jsonMapper;
        this.responseFactory = responseFactory;
        this.metricsService = metricsService;
    }

    private ReceberAvaliacaoUseCase useCase() {
        if (useCase != null) {
            return useCase;
        }
        return config.receberAvaliacaoUseCase();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        System.out.println("[FeedbackReceiverHandler] handleRequest start");
        LOGGER.info("Recebendo POST /avaliacao requestId={}",
                context != null ? context.getAwsRequestId() : "local");

        try {
            AvaliacaoRequest request = jsonMapper.fromJson(input != null ? input.getBody() : null, AvaliacaoRequest.class);
            AvaliacaoResponse response = useCase().executar(request);

            metricsService.incrementarContador(METRIC_SUCESSO);
            LOGGER.info("Avaliação aceita id={} urgencia={}", response.getId(), response.getUrgencia());
            return responseFactory.sucesso(201, response);

        } catch (AvaliacaoInvalidaException e) {
            metricsService.incrementarContador(METRIC_ERRO_VALIDACAO);
            LOGGER.warn("Avaliação inválida: {}", e.getMessage());
            return responseFactory.erro(400, e.getMessage());

        } catch (Exception e) {
            metricsService.incrementarContador(METRIC_ERRO_INTERNO);
            LOGGER.error("Erro ao processar avaliação", e);
            e.printStackTrace();
            return responseFactory.erro(500, "Erro interno ao processar a avaliação: " + e.getMessage());
        }
    }

}
