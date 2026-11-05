
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
 *
 * Responsabilidade:
 * - Receber a requisicao HTTP
 * - Executar o caso de uso
 * - Registrar metricas e logs
 * - Retornar respostas HTTP seguras
 */
public class FeedbackReceiverHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FeedbackReceiverHandler.class);

    private static final String METRIC_SUCESSO =
            "avaliacao.recebida.sucesso";

    private static final String METRIC_ERRO_VALIDACAO =
            "avaliacao.recebida.erro_validacao";

    private static final String METRIC_ERRO_INTERNO =
            "avaliacao.recebida.erro_interno";

    private static final String MENSAGEM_ERRO_INTERNO =
            "Erro interno ao processar a avaliação.";

    private final FeedbackReceiverConfig config;
    private final JsonMapper jsonMapper;
    private final ApiGatewayResponseFactory responseFactory;
    private final MetricsService metricsService;
    private final ReceberAvaliacaoUseCase useCase;

    /**
     * Construtor utilizado pela AWS Lambda.
     */
    public FeedbackReceiverHandler() {
        LOGGER.info("Inicializando FeedbackReceiverHandler");

        this.config = new FeedbackReceiverConfig();
        this.jsonMapper = config.jsonMapper();
        this.responseFactory = config.responseFactory();
        this.metricsService = config.metricsService();

        // DynamoDB inicializado apenas quando necessario.
        this.useCase = null;

        LOGGER.info("FeedbackReceiverHandler inicializado");
    }

    /**
     * Construtor utilizado pelos testes automatizados.
     */
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

    /**
     * Recupera o caso de uso, mantendo a inicializacao lazy.
     */
    private ReceberAvaliacaoUseCase useCase() {
        if (useCase != null) {
            return useCase;
        }

        return config.receberAvaliacaoUseCase();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input,
            Context context) {

        String requestId = context != null
                ? context.getAwsRequestId()
                : "local";

        LOGGER.info(
                "Recebendo POST /avaliacao requestId={}",
                requestId
        );

        try {
            AvaliacaoRequest request = jsonMapper.fromJson(
                    input != null ? input.getBody() : null,
                    AvaliacaoRequest.class
            );

            AvaliacaoResponse response = useCase().executar(request);

            // Registra sucesso no CloudWatch EMF.
            metricsService.incrementarContador(METRIC_SUCESSO);

            LOGGER.info(
                    "Avaliação aceita id={} urgencia={}",
                    response.getId(),
                    response.getUrgencia()
            );

            return responseFactory.sucesso(201, response);

        } catch (AvaliacaoInvalidaException e) {

            // Registra erro de validacao.
            metricsService.incrementarContador(METRIC_ERRO_VALIDACAO);

            LOGGER.warn(
                    "Avaliação inválida requestId={}: {}",
                    requestId,
                    e.getMessage()
            );

            return responseFactory.erro(400, e.getMessage());

        } catch (Exception e) {

            // Registra erro interno para monitoramento.
            metricsService.incrementarContador(METRIC_ERRO_INTERNO);

            // Detalhes tecnicos permanecem somente nos logs.
            LOGGER.error(
                    "Erro interno ao processar avaliação requestId={}",
                    requestId,
                    e
            );

            // Nunca retorna detalhes internos da excecao ao cliente.
            return responseFactory.erro(
                    500,
                    MENSAGEM_ERRO_INTERNO
            );
        }
    }
}
