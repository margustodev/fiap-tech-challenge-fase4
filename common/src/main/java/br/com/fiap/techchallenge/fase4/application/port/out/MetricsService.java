package br.com.fiap.techchallenge.fase4.application.port.out;

public interface MetricsService {

    void incrementarContador(String nomeMetrica);

    void registrarValor(String nomeMetrica, double valor);
}