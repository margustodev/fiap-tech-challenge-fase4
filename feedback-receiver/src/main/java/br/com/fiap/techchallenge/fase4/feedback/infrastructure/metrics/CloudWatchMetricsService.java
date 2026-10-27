package br.com.fiap.techchallenge.fase4.feedback.infrastructure.metrics;

import br.com.fiap.techchallenge.fase4.application.port.out.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptador de métricas. Publica em log estruturado (CloudWatch Logs captura automaticamente).
 * Pode evoluir para PutMetricData do CloudWatch Metrics.
 */
public class CloudWatchMetricsService implements MetricsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudWatchMetricsService.class);

    @Override
    public void incrementarContador(String nomeMetrica) {
        LOGGER.info("metric_counter name={} value=1", nomeMetrica);
    }

    @Override
    public void registrarValor(String nomeMetrica, double valor) {
        LOGGER.info("metric_value name={} value={}", nomeMetrica, valor);
    }

}
