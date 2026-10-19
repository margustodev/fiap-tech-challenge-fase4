package br.com.fiap.techchallenge.fase4.application.mapper;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoRequest;
import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResponse;
import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResumoDto;
import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;

/**
 * Mapper entre DTOs e entidade de domínio Avaliacao.
 */
public class AvaliacaoMapper {

    public Avaliacao toDomain(AvaliacaoRequest request) {
        // TODO: mapear request para domínio
        return null;
    }

    public AvaliacaoResponse toResponse(Avaliacao avaliacao) {
        // TODO: mapear domínio para response
        return null;
    }

    public NotificacaoUrgenciaDto toNotificacaoDto(Avaliacao avaliacao) {
        // TODO: mapear domínio para DTO de notificação
        return null;
    }

    public AvaliacaoResumoDto toResumoDto(Avaliacao avaliacao) {
        // TODO: mapear domínio para resumo do relatório
        return null;
    }

}
