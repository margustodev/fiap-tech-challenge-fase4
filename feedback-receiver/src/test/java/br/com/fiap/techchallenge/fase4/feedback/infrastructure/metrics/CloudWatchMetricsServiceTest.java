
package br.com.fiap.techchallenge.fase4.feedback.infrastructure.metrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CloudWatchMetricsServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PrintStream saidaOriginal;
    private ByteArrayOutputStream saidaCapturada;
    private CloudWatchMetricsService metricsService;

    @BeforeEach
    void prepararTeste() {
        saidaOriginal = System.out;
        saidaCapturada = new ByteArrayOutputStream();

        System.setOut(new PrintStream(
                saidaCapturada,
                true,
                StandardCharsets.UTF_8
        ));

        metricsService = new CloudWatchMetricsService();
    }

    @AfterEach
    void finalizarTeste() {
        System.setOut(saidaOriginal);
    }

    @Test
    void deveRegistrarContadorNoFormatoEmf() throws Exception {
        metricsService.incrementarContador("avaliacao.recebida.sucesso");

        JsonNode evento = obterEvento();

        assertEquals(
                1.0,
                evento.path("avaliacao.recebida.sucesso").asDouble()
        );

        assertEquals(
                "feedback-receiver",
                evento.path("Service").asText()
        );

        JsonNode definicao = evento.path("_aws")
                .path("CloudWatchMetrics").get(0);

        assertEquals(
                "FIAP/FeedbackCursos",
                definicao.path("Namespace").asText()
        );

        assertEquals(
                "avaliacao.recebida.sucesso",
                definicao.path("Metrics").get(0)
                        .path("Name").asText()
        );

        assertEquals(
                "Count",
                definicao.path("Metrics").get(0)
                        .path("Unit").asText()
        );

        assertEquals(
                "Service",
                definicao.path("Dimensions").get(0)
                        .get(0).asText()
        );

        assertTrue(evento.path("_aws")
                .path("Timestamp").asLong() > 0);
    }

    @Test
    void deveRegistrarValorNoFormatoEmf() throws Exception {
        metricsService.registrarValor("tempo.processamento", 25.5);

        JsonNode evento = obterEvento();

        assertEquals(
                25.5,
                evento.path("tempo.processamento").asDouble()
        );

        JsonNode metrica = evento.path("_aws")
                .path("CloudWatchMetrics").get(0)
                .path("Metrics").get(0);

        assertEquals("tempo.processamento",
                metrica.path("Name").asText());

        assertEquals("None",
                metrica.path("Unit").asText());
    }

    @Test
    void naoDevePublicarMetricaComNomeInvalido() {
        metricsService.incrementarContador(null);
        metricsService.incrementarContador("");
        metricsService.incrementarContador("Service");
        metricsService.incrementarContador("_aws");

        assertTrue(saidaCapturada.toString(
                StandardCharsets.UTF_8).isBlank());
    }

    @Test
    void naoDevePublicarValorNumericoInvalido() {
        metricsService.registrarValor(
                "tempo.processamento",
                Double.NaN
        );

        metricsService.registrarValor(
                "tempo.processamento",
                Double.POSITIVE_INFINITY
        );

        assertTrue(saidaCapturada.toString(
                StandardCharsets.UTF_8).isBlank());
    }

    private JsonNode obterEvento() throws Exception {
        String json = saidaCapturada.toString(
                StandardCharsets.UTF_8
        ).trim();

        assertFalse(json.isBlank());

        return objectMapper.readTree(json);
    }
}
