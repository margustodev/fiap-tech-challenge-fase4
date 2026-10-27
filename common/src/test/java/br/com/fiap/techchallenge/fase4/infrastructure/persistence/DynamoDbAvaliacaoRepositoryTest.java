package br.com.fiap.techchallenge.fase4.infrastructure.persistence;

import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoDbAvaliacaoRepositoryTest {

    @Mock
    private DynamoDbTable<AvaliacaoEntity> table;

    @Mock
    private DynamoDbIndex<AvaliacaoEntity> periodoIndex;

    private DynamoDbAvaliacaoRepository repository;
    private final AvaliacaoEntityMapper mapper = new AvaliacaoEntityMapper();

    @BeforeEach
    void setUp() {
        repository = new DynamoDbAvaliacaoRepository(table, periodoIndex, mapper);
    }

    @Test
    void devePersistirAvaliacao() {
        Avaliacao avaliacao = new Avaliacao(
                "av-1",
                "Material desatualizado",
                3,
                Urgencia.ALTA,
                Instant.parse("2026-10-27T12:00:00Z")
        );

        Avaliacao salva = repository.salvar(avaliacao);

        ArgumentCaptor<AvaliacaoEntity> captor = ArgumentCaptor.forClass(AvaliacaoEntity.class);
        verify(table).putItem(captor.capture());
        assertEquals("av-1", captor.getValue().getId());
        assertEquals(AvaliacoesTableSchema.ENTITY_TYPE_AVALIACAO, captor.getValue().getEntityType());
        assertEquals("ALTA", captor.getValue().getUrgencia());
        assertEquals("av-1", salva.getId());
        assertEquals(Urgencia.ALTA, salva.getUrgencia());
    }

    @Test
    void deveBuscarPorId() {
        AvaliacaoEntity entity = new AvaliacaoEntity();
        entity.setId("av-2");
        entity.setDescricao("Bom curso");
        entity.setNota(9);
        entity.setUrgencia("BAIXA");
        entity.setDataEnvio("2026-10-27T10:00:00Z");
        entity.setEntityType(AvaliacoesTableSchema.ENTITY_TYPE_AVALIACAO);

        when(table.getItem(any(Key.class))).thenReturn(entity);

        Optional<Avaliacao> encontrada = repository.buscarPorId("av-2");

        assertTrue(encontrada.isPresent());
        assertEquals(9, encontrada.get().getNota());
        assertEquals(Urgencia.BAIXA, encontrada.get().getUrgencia());
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExiste() {
        when(table.getItem(any(Key.class))).thenReturn(null);

        Optional<Avaliacao> encontrada = repository.buscarPorId("inexistente");

        assertTrue(encontrada.isEmpty());
    }

    @Test
    void deveRejeitarPeriodoInvalido() {
        Instant inicio = Instant.parse("2026-10-27T00:00:00Z");
        Instant fim = Instant.parse("2026-10-20T00:00:00Z");

        assertThrows(IllegalArgumentException.class,
                () -> repository.listarPorPeriodo(inicio, fim));
    }

}
