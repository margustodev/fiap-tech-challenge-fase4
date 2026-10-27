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
        return new Avaliacao(
                null,
                request.getDescricao(),
                request.getNota(),
                null,
                null
        );
    }

    public AvaliacaoResponse toResponse(Avaliacao avaliacao) {
        // TODO: mapear domínio para response
        return new AvaliacaoResponse(
                avaliacao.getId(),
                avaliacao.getDescricao(),
                avaliacao.getNota(),
                avaliacao.getUrgencia(),
                avaliacao.getDataEnvio()
        );
    }

    public NotificacaoUrgenciaDto toNotificacaoDto(Avaliacao avaliacao) {
        // TODO: mapear domínio para DTO de notificação
        return new NotificacaoUrgenciaDto(
                avaliacao.getDescricao(),
                avaliacao.getUrgencia(),
                avaliacao.getDataEnvio()
        );
    }

    public AvaliacaoResumoDto toResumoDto(Avaliacao avaliacao) {
        // TODO: mapear domínio para resumo do relatório
        return new AvaliacaoResumoDto(
                avaliacao.getDescricao(),
                avaliacao.getUrgencia(),
                avaliacao.getDataEnvio()
        );
    }

}
