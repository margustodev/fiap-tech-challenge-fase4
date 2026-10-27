package br.com.fiap.techchallenge.fase4.application.usecase;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoRequest;
import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResponse;
import br.com.fiap.techchallenge.fase4.application.exception.AvaliacaoInvalidaException;
import br.com.fiap.techchallenge.fase4.application.mapper.AvaliacaoMapper;
import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.application.validator.AvaliacaoRequestValidator;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.service.CalcularUrgenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceberAvaliacaoUseCaseTest {

    private InMemoryAvaliacaoRepository repository;
    private ReceberAvaliacaoUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAvaliacaoRepository();
        useCase = new ReceberAvaliacaoUseCase(
                new AvaliacaoRequestValidator(),
                new CalcularUrgenciaService(),
                new AvaliacaoMapper(),
                repository
        );
    }

    @Test
    void deveValidarCalcularUrgenciaEPersistir() {
        AvaliacaoResponse response = useCase.executar(
                new AvaliacaoRequest("Material desatualizado", 2)
        );

        assertEquals(1, repository.salvas.size());
        Avaliacao salva = repository.salvas.get(0);

        assertNotNull(salva.getId());
        assertEquals(Urgencia.CRITICA, salva.getUrgencia());
        assertEquals(2, salva.getNota());
        assertEquals("Material desatualizado", salva.getDescricao());
        assertNotNull(salva.getDataEnvio());

        assertEquals(salva.getId(), response.getId());
        assertEquals(Urgencia.CRITICA, response.getUrgencia());
        assertTrue(repository.buscarPorId(salva.getId()).isPresent());
    }

    @Test
    void devePropagarErroDeValidacao() {
        assertThrows(AvaliacaoInvalidaException.class,
                () -> useCase.executar(new AvaliacaoRequest("", 5)));
        assertTrue(repository.salvas.isEmpty());
    }

    private static final class InMemoryAvaliacaoRepository implements AvaliacaoRepository {

        private final List<Avaliacao> salvas = new ArrayList<>();

        @Override
        public Avaliacao salvar(Avaliacao avaliacao) {
            salvas.add(avaliacao);
            return avaliacao;
        }

        @Override
        public Optional<Avaliacao> buscarPorId(String id) {
            return salvas.stream().filter(a -> a.getId().equals(id)).findFirst();
        }

        @Override
        public List<Avaliacao> listarPorPeriodo(Instant inicio, Instant fim) {
            return List.of();
        }
    }

}
