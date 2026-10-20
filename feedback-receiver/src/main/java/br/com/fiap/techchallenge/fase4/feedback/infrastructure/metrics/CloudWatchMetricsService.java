package br.com.fiap.techchallenge.fase4.feedback.infrastructure.metrics;

import br.com.fiap.techchallenge.fase4.application.port.out.MetricsService;

/**
 * Adaptador de métricas via Amazon CloudWatch.
 */
public class CloudWatchMetricsService implements MetricsService {

    @Override
    public void incrementarContador(String nomeMetrica) {
        // TODO: publicar métrica de contador no CloudWatch
    }

    @Override
    public void registrarValor(String nomeMetrica, double valor) {
        // TODO: publicar valor de métrica no CloudWatch
    }

}
