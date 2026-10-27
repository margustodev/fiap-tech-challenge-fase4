package br.com.fiap.techchallenge.fase4.application.usecase;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoRequest;
import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResponse;
import br.com.fiap.techchallenge.fase4.application.mapper.AvaliacaoMapper;
import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.application.validator.AvaliacaoRequestValidator;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.service.CalcularUrgenciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso: receber, validar e persistir uma avaliação.
 */
public class ReceberAvaliacaoUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceberAvaliacaoUseCase.class);

    private final AvaliacaoRequestValidator validator;
    private final CalcularUrgenciaService calcularUrgenciaService;
    private final AvaliacaoMapper mapper;
    private final AvaliacaoRepository repository;

    public ReceberAvaliacaoUseCase(
            AvaliacaoRequestValidator validator,
            CalcularUrgenciaService calcularUrgenciaService,
            AvaliacaoMapper mapper,
            AvaliacaoRepository repository) {
        this.validator = Objects.requireNonNull(validator);
        this.calcularUrgenciaService = Objects.requireNonNull(calcularUrgenciaService);
        this.mapper = Objects.requireNonNull(mapper);
        this.repository = Objects.requireNonNull(repository);
    }

    public AvaliacaoResponse executar(AvaliacaoRequest request) {
        validator.validar(request);

        String id = UUID.randomUUID().toString();
        Instant dataEnvio = Instant.now();
        Urgencia urgencia = calcularUrgenciaService.calcular(request.getNota());

        Avaliacao avaliacao = mapper.toDomain(request, id, urgencia, dataEnvio);
        Avaliacao salva = repository.salvar(avaliacao);

        LOGGER.info(
                "Avaliação persistida id={} nota={} urgencia={}",
                salva.getId(),
                salva.getNota(),
                salva.getUrgencia()
        );

        return mapper.toResponse(salva);
    }

}
