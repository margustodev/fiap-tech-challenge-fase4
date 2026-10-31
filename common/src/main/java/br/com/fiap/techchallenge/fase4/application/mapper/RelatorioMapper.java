
package br.com.fiap.techchallenge.fase4.application.mapper;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResumoDto;
import br.com.fiap.techchallenge.fase4.application.dto.RelatorioSemanalDto;
import br.com.fiap.techchallenge.fase4.domain.model.RelatorioSemanal;

import java.util.List;

/**
 * Responsável por converter o relatório semanal
 * do domínio para o DTO utilizado na apresentação
 * e envio aos administradores.
 */
public class RelatorioMapper {

    private final AvaliacaoMapper avaliacaoMapper =
            new AvaliacaoMapper();

    public RelatorioSemanalDto toDto(RelatorioSemanal relatorio) {

        if (relatorio == null) {
            throw new IllegalArgumentException(
                    "O relatório semanal não pode ser nulo."
            );
        }

        List<AvaliacaoResumoDto> resumos =
                relatorio.getAvaliacoes() == null
                        ? List.of()
                        : relatorio.getAvaliacoes()
                        .stream()
                        .map(avaliacaoMapper::toResumoDto)
                        .toList();

        return new RelatorioSemanalDto(
                relatorio.getDataInicio(),
                relatorio.getDataFim(),
                relatorio.getMediaAvaliacoes(),
                relatorio.getTotalAvaliacoes(),
                resumos,
                relatorio.getQuantidadePorDia() == null
                        ? List.of()
                        : relatorio.getQuantidadePorDia(),
                relatorio.getQuantidadePorUrgencia() == null
                        ? List.of()
                        : relatorio.getQuantidadePorUrgencia()
        );
    }
}
