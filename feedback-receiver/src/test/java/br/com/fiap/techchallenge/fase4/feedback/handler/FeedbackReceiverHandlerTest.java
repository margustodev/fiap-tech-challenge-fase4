package br.com.fiap.techchallenge.fase4.feedback.handler;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoRequest;
import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResponse;
import br.com.fiap.techchallenge.fase4.application.exception.AvaliacaoInvalidaException;
import br.com.fiap.techchallenge.fase4.application.mapper.AvaliacaoMapper;
import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.application.port.out.MetricsService;
import br.com.fiap.techchallenge.fase4.application.usecase.ReceberAvaliacaoUseCase;
import br.com.fiap.techchallenge.fase4.application.validator.AvaliacaoRequestValidator;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.service.CalcularUrgenciaService;
import br.com.fiap.techchallenge.fase4.feedback.infrastructure.http.ApiGatewayResponseFactory;
import br.com.fiap.techchallenge.fase4.infrastructure.util.JsonMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedbackReceiverHandlerTest {

    private FakeReceberAvaliacaoUseCase useCase;
    private RecordingMetricsService metricsService;
    private FeedbackReceiverHandler handler;

    @BeforeEach
    void setUp() {
        useCase = new FakeReceberAvaliacaoUseCase();
        metricsService = new RecordingMetricsService();
        JsonMapper jsonMapper = new JsonMapper();
        handler = new FeedbackReceiverHandler(
                useCase,
                jsonMapper,
                new ApiGatewayResponseFactory(jsonMapper),
                metricsService
        );
    }

    @Test
    void deveRetornar201QuandoSucesso() {
        useCase.response = new AvaliacaoResponse(
                "id-1", "Curso ok", 9, Urgencia.BAIXA, Instant.parse("2026-10-27T12:00:00Z")
        );

        APIGatewayProxyResponseEvent response = handler.handleRequest(
                new APIGatewayProxyRequestEvent().withBody("{\"descricao\":\"Curso ok\",\"nota\":9}"),
                fakeContext()
        );

        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().contains("id-1"));
        assertTrue(response.getBody().contains("BAIXA"));
        assertTrue(metricsService.contadores.contains("avaliacao.recebida.sucesso"));
    }

    @Test
    void deveRetornar400QuandoInvalido() {
        useCase.erroValidacao = new AvaliacaoInvalidaException(
                "O campo 'nota' deve estar entre 0 e 10."
        );

        APIGatewayProxyResponseEvent response = handler.handleRequest(
                new APIGatewayProxyRequestEvent().withBody("{\"descricao\":\"x\",\"nota\":99}"),
                fakeContext()
        );

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("nota"));
        assertTrue(metricsService.contadores.contains("avaliacao.recebida.erro_validacao"));
    }

    @Test
    void deveRetornar400QuandoJsonInvalido() {
        APIGatewayProxyResponseEvent response = handler.handleRequest(
                new APIGatewayProxyRequestEvent().withBody("{invalido"),
                fakeContext()
        );

        assertEquals(400, response.getStatusCode());
        assertTrue(metricsService.contadores.contains("avaliacao.recebida.erro_validacao"));
    }

    @Test
    void deveRetornar500QuandoErroInesperado() {
        useCase.erroInterno = new RuntimeException("falha dynamodb");

        APIGatewayProxyResponseEvent response = handler.handleRequest(
                new APIGatewayProxyRequestEvent().withBody("{\"descricao\":\"x\",\"nota\":5}"),
                fakeContext()
        );

        assertEquals(500, response.getStatusCode());
        assertTrue(metricsService.contadores.contains("avaliacao.recebida.erro_interno"));
    }

    private static Context fakeContext() {
        return new Context() {
            @Override
            public String getAwsRequestId() {
                return "req-1";
            }

            @Override
            public String getLogGroupName() {
                return null;
            }

            @Override
            public String getLogStreamName() {
                return null;
            }

            @Override
            public String getFunctionName() {
                return "feedback-receiver";
            }

            @Override
            public String getFunctionVersion() {
                return "1";
            }

            @Override
            public String getInvokedFunctionArn() {
                return null;
            }

            @Override
            public com.amazonaws.services.lambda.runtime.CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public com.amazonaws.services.lambda.runtime.ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 30000;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 512;
            }

            @Override
            public com.amazonaws.services.lambda.runtime.LambdaLogger getLogger() {
                return null;
            }
        };
    }

    private static final class FakeReceberAvaliacaoUseCase extends ReceberAvaliacaoUseCase {

        private AvaliacaoResponse response;
        private AvaliacaoInvalidaException erroValidacao;
        private RuntimeException erroInterno;

        private FakeReceberAvaliacaoUseCase() {
            super(
                    new AvaliacaoRequestValidator(),
                    new CalcularUrgenciaService(),
                    new AvaliacaoMapper(),
                    new AvaliacaoRepository() {
                        @Override
                        public Avaliacao salvar(Avaliacao avaliacao) {
                            return avaliacao;
                        }

                        @Override
                        public Optional<Avaliacao> buscarPorId(String id) {
                            return Optional.empty();
                        }

                        @Override
                        public List<Avaliacao> listarPorPeriodo(Instant inicio, Instant fim) {
                            return List.of();
                        }
                    }
            );
        }

        @Override
        public AvaliacaoResponse executar(AvaliacaoRequest request) {
            if (erroValidacao != null) {
                throw erroValidacao;
            }
            if (erroInterno != null) {
                throw erroInterno;
            }
            return response;
        }
    }

    private static final class RecordingMetricsService implements MetricsService {

        private final List<String> contadores = new ArrayList<>();

        @Override
        public void incrementarContador(String nomeMetrica) {
            contadores.add(nomeMetrica);
        }

        @Override
        public void registrarValor(String nomeMetrica, double valor) {
            // não usado nestes testes
        }
    }

}
