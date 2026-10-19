package br.com.fiap.techchallenge.fase4.report.infrastructure.report;

import br.com.fiap.techchallenge.fase4.application.dto.RelatorioSemanalDto;
import br.com.fiap.techchallenge.fase4.application.port.out.RelatorioService;

/**
 * Adaptador que envia o relatório semanal por e-mail via Amazon SES.
 */
public class SesRelatorioService implements RelatorioService {

    @Override
    public void enviarRelatorio(RelatorioSemanalDto relatorio) {
        // TODO: formatar e enviar relatório (média, por dia, por urgência)
    }

}
