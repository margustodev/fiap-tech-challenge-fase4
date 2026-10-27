package br.com.fiap.techchallenge.fase4.infrastructure.persistence;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;

import java.time.Instant;

/**
 * Conversão entre domínio {@link Avaliacao} e item DynamoDB {@link AvaliacaoEntity}.
 */
public class AvaliacaoEntityMapper {

    public AvaliacaoEntity toEntity(Avaliacao avaliacao) {
        if (avaliacao == null) {
            return null;
        }
        AvaliacaoEntity entity = new AvaliacaoEntity();
        entity.setId(avaliacao.getId());
        entity.setDescricao(avaliacao.getDescricao());
        entity.setNota(avaliacao.getNota());
        entity.setUrgencia(avaliacao.getUrgencia() != null ? avaliacao.getUrgencia().name() : null);
        entity.setDataEnvio(avaliacao.getDataEnvio() != null ? avaliacao.getDataEnvio().toString() : null);
        entity.setEntityType(AvaliacoesTableSchema.ENTITY_TYPE_AVALIACAO);
        return entity;
    }

    public Avaliacao toDomain(AvaliacaoEntity entity) {
        if (entity == null) {
            return null;
        }
        Urgencia urgencia = entity.getUrgencia() != null ? Urgencia.valueOf(entity.getUrgencia()) : null;
        Instant dataEnvio = entity.getDataEnvio() != null ? Instant.parse(entity.getDataEnvio()) : null;
        return new Avaliacao(
                entity.getId(),
                entity.getDescricao(),
                entity.getNota(),
                urgencia,
                dataEnvio
        );
    }

}
