package br.com.fiap.techchallenge.fase4.infrastructure.persistence;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AvaliacaoEntityMapperTest {

    private final AvaliacaoEntityMapper mapper = new AvaliacaoEntityMapper();

    @Test
    void deveConverterDominioParaEntityEVoltar() {
        Instant agora = Instant.parse("2026-10-27T15:00:00Z");
        Avaliacao original = new Avaliacao("id-1", "Curso incompleto", 2, Urgencia.CRITICA, agora);

        AvaliacaoEntity entity = mapper.toEntity(original);
        Avaliacao reconstruida = mapper.toDomain(entity);

        assertEquals("id-1", entity.getId());
        assertEquals(AvaliacoesTableSchema.ENTITY_TYPE_AVALIACAO, entity.getEntityType());
        assertEquals("CRITICA", entity.getUrgencia());
        assertEquals("2026-10-27T15:00:00Z", entity.getDataEnvio());

        assertEquals(original.getId(), reconstruida.getId());
        assertEquals(original.getDescricao(), reconstruida.getDescricao());
        assertEquals(original.getNota(), reconstruida.getNota());
        assertEquals(original.getUrgencia(), reconstruida.getUrgencia());
        assertEquals(original.getDataEnvio(), reconstruida.getDataEnvio());
    }

    @Test
    void deveRetornarNullParaEntradaNull() {
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toDomain(null));
    }

}
