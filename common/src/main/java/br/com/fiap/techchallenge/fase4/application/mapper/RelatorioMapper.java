package br.com.fiap.techchallenge.fase4.application.mapper;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResumoDto;
import br.com.fiap.techchallenge.fase4.application.dto.RelatorioSemanalDto;
import br.com.fiap.techchallenge.fase4.domain.model.RelatorioSemanal;

import java.util.List;

/**
 * Mapper entre o agregado RelatorioSemanal e seu DTO.
 */
public class RelatorioMapper {

    public RelatorioSemanalDto toDto(RelatorioSemanal relatorio) {
        // TODO: mapear domínio para DTO do relatório
        AvaliacaoMapper avaliacaoMapper = new AvaliacaoMapper();

        List<AvaliacaoResumoDto> avaliacoes = relatorio.getAvaliacoes()
                .stream()
                .map(avaliacaoMapper::toResumoDto)
                .toList();

        return new RelatorioSemanalDto(
                relatorio.getDataInicio(),
                relatorio.getDataFim(),
                relatorio.getMediaAvaliacoes(),
                relatorio.getTotalAvaliacoes(),
                avaliacoes,
                relatorio.getQuantidadePorDia(),
                relatorio.getQuantidadePorUrgencia()
        );
    }

}
