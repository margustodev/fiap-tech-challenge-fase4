package br.com.fiap.techchallenge.fase4.application.port.out;

/**
 * Porta de saída para publicação de métricas de monitoramento.
 */
public interface MetricsService {

    void incrementarContador(String nomeMetrica);

    void registrarValor(String nomeMetrica, double valor);

}
