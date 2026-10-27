package br.com.fiap.techchallenge.fase4.application.mapper;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoRequest;
import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResponse;
import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResumoDto;
import br.com.fiap.techchallenge.fase4.application.dto.NotificacaoUrgenciaDto;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;

import java.time.Instant;

/**
 * Mapper entre DTOs e entidade de domínio Avaliacao.
 */
public class AvaliacaoMapper {

    public Avaliacao toDomain(
            AvaliacaoRequest request,
            String id,
            Urgencia urgencia,
            Instant dataEnvio) {
        return new Avaliacao(
                id,
                request.getDescricao().trim(),
                request.getNota(),
                urgencia,
                dataEnvio
        );
    }

    public AvaliacaoResponse toResponse(Avaliacao avaliacao) {
        return new AvaliacaoResponse(
                avaliacao.getId(),
                avaliacao.getDescricao(),
                avaliacao.getNota(),
                avaliacao.getUrgencia(),
                avaliacao.getDataEnvio()
        );
    }

    public NotificacaoUrgenciaDto toNotificacaoDto(Avaliacao avaliacao) {
        return new NotificacaoUrgenciaDto(
                avaliacao.getDescricao(),
                avaliacao.getUrgencia(),
                avaliacao.getDataEnvio()
        );
    }

    public AvaliacaoResumoDto toResumoDto(Avaliacao avaliacao) {
        return new AvaliacaoResumoDto(
                avaliacao.getDescricao(),
                avaliacao.getUrgencia(),
                avaliacao.getDataEnvio()
        );
    }

}
