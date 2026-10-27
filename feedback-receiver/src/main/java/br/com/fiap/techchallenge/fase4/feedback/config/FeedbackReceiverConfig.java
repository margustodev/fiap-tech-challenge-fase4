package br.com.fiap.techchallenge.fase4.feedback.config;

import br.com.fiap.techchallenge.fase4.application.mapper.AvaliacaoMapper;
import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.application.port.out.MetricsService;
import br.com.fiap.techchallenge.fase4.application.usecase.ReceberAvaliacaoUseCase;
import br.com.fiap.techchallenge.fase4.application.validator.AvaliacaoRequestValidator;
import br.com.fiap.techchallenge.fase4.domain.service.CalcularUrgenciaService;
import br.com.fiap.techchallenge.fase4.feedback.infrastructure.http.ApiGatewayResponseFactory;
import br.com.fiap.techchallenge.fase4.feedback.infrastructure.metrics.CloudWatchMetricsService;
import br.com.fiap.techchallenge.fase4.infrastructure.config.AppConfig;
import br.com.fiap.techchallenge.fase4.infrastructure.persistence.DynamoDbAvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.infrastructure.persistence.DynamoDbClientFactory;
import br.com.fiap.techchallenge.fase4.infrastructure.util.JsonMapper;

/**
 * Montagem de dependências da função feedback-receiver.
 */
public class FeedbackReceiverConfig {

    private final AppConfig appConfig = new AppConfig();
    private final JsonMapper jsonMapper = new JsonMapper();
    private final AvaliacaoMapper avaliacaoMapper = new AvaliacaoMapper();
    private final AvaliacaoRequestValidator validator = new AvaliacaoRequestValidator();
    private final CalcularUrgenciaService calcularUrgenciaService = new CalcularUrgenciaService();
    private final DynamoDbClientFactory clientFactory = new DynamoDbClientFactory(appConfig);

    private AvaliacaoRepository avaliacaoRepository;
    private ReceberAvaliacaoUseCase receberAvaliacaoUseCase;

    public AppConfig appConfig() {
        return appConfig;
    }

    public JsonMapper jsonMapper() {
        return jsonMapper;
    }

    public ApiGatewayResponseFactory responseFactory() {
        return new ApiGatewayResponseFactory(jsonMapper);
    }

    public MetricsService metricsService() {
        return new CloudWatchMetricsService();
    }

    public AvaliacaoRepository avaliacaoRepository() {
        if (avaliacaoRepository == null) {
            avaliacaoRepository = new DynamoDbAvaliacaoRepository(
                    clientFactory.createEnhancedClient(),
                    appConfig
            );
        }
        return avaliacaoRepository;
    }

    public ReceberAvaliacaoUseCase receberAvaliacaoUseCase() {
        if (receberAvaliacaoUseCase == null) {
            receberAvaliacaoUseCase = new ReceberAvaliacaoUseCase(
                    validator,
                    calcularUrgenciaService,
                    avaliacaoMapper,
                    avaliacaoRepository()
            );
        }
        return receberAvaliacaoUseCase;
    }

}
