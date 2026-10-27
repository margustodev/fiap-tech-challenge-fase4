package br.com.fiap.techchallenge.fase4.application.usecase;

import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoRequest;
import br.com.fiap.techchallenge.fase4.application.dto.AvaliacaoResponse;
import br.com.fiap.techchallenge.fase4.application.mapper.AvaliacaoMapper;
import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.application.validator.AvaliacaoRequestValidator;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.domain.service.CalcularUrgenciaService;

import java.time.Instant;
import java.util.UUID;

public class ReceberAvaliacaoUseCase {

    private final AvaliacaoRepository avaliacaoRepository;
    private final AvaliacaoRequestValidator validator;
    private final CalcularUrgenciaService calcularUrgenciaService;
    private final AvaliacaoMapper avaliacaoMapper;

    public ReceberAvaliacaoUseCase(
            AvaliacaoRepository avaliacaoRepository,
            AvaliacaoRequestValidator validator,
            CalcularUrgenciaService calcularUrgenciaService,
            AvaliacaoMapper avaliacaoMapper) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.validator = validator;
        this.calcularUrgenciaService = calcularUrgenciaService;
        this.avaliacaoMapper = avaliacaoMapper;
    }

    public AvaliacaoResponse executar(AvaliacaoRequest request) {
        validator.validar(request);

        Avaliacao dados = avaliacaoMapper.toDomain(request);

        Avaliacao avaliacao = new Avaliacao(
                UUID.randomUUID().toString(),
                dados.getDescricao(),
                dados.getNota(),
                calcularUrgenciaService.calcular(dados.getNota()),
                Instant.now()
        );

        Avaliacao avaliacaoSalva = avaliacaoRepository.salvar(avaliacao);

        return avaliacaoMapper.toResponse(avaliacaoSalva);
    }
}