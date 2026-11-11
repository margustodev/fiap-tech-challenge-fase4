
package br.com.fiap.techchallenge.fase4.feedback.infrastructure.metrics;

import br.com.fiap.techchallenge.fase4.application.port.out.MetricsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publica metricas personalizadas no formato CloudWatch EMF.
 *
 * As metricas sao registradas como logs JSON estruturados.
 * Na AWS, o CloudWatch Logs pode extrair esses valores
 * e disponibiliza-los no CloudWatch Metrics.
 */
public class CloudWatchMetricsService implements MetricsService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CloudWatchMetricsService.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String NAMESPACE = "FIAP/FeedbackCursos";
    private static final String SERVICE_NAME = "feedback-receiver";

    @Override
    public void incrementarContador(String nomeMetrica) {
        publicarMetrica(nomeMetrica, 1, "Count");
    }

    @Override
    public void registrarValor(String nomeMetrica, double valor) {
        publicarMetrica(nomeMetrica, valor, "None");
    }

    /**
     * Gera um evento JSON seguindo a especificacao EMF da AWS.
     */
    private void publicarMetrica(String nomeMetrica, double valor, String unidade) {

        if (nomeMetrica == null || nomeMetrica.isBlank()
                || nomeMetrica.equals("_aws")
                || nomeMetrica.equals("Service")) {
            LOGGER.warn("Nome de metrica invalido: {}", nomeMetrica);
            return;
        }

        if (!Double.isFinite(valor)) {
            LOGGER.warn("Valor invalido para a metrica: {}", nomeMetrica);
            return;
        }

        ObjectNode evento = OBJECT_MAPPER.createObjectNode();

        // Metadados exigidos pelo CloudWatch Embedded Metric Format.
        ObjectNode aws = evento.putObject("_aws");
        aws.put("Timestamp", System.currentTimeMillis());

        ObjectNode definicao = aws
                .putArray("CloudWatchMetrics")
                .addObject();

        definicao.put("Namespace", NAMESPACE);

        definicao.putArray("Dimensions")
                .addArray()
                .add("Service");

        ObjectNode metrica = definicao
                .putArray("Metrics")
                .addObject();

        metrica.put("Name", nomeMetrica);
        metrica.put("Unit", unidade);

        // Dimensao e valor numerico da metrica.
        evento.put("Service", SERVICE_NAME);
        evento.put(nomeMetrica, valor);

        // O EMF precisa ser uma linha JSON sem prefixos de log.
        System.out.println(evento.toString());
    }
}
