package br.com.fiap.techchallenge.fase4.application.port.out;

import br.com.fiap.techchallenge.fase4.application.dto.RelatorioSemanalDto;

/**
 * Porta de saída para entrega do relatório semanal.
 */
public interface RelatorioService {

    void enviarRelatorio(RelatorioSemanalDto relatorio);

}
