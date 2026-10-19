package br.com.fiap.techchallenge.fase4.feedback.infrastructure.persistence;

import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistência de avaliações no Amazon DynamoDB.
 */
public class DynamoDbAvaliacaoRepository implements AvaliacaoRepository {

    @Override
    public Avaliacao salvar(Avaliacao avaliacao) {
        // TODO: persistir avaliação no DynamoDB
        return null;
    }

    @Override
    public Optional<Avaliacao> buscarPorId(String id) {
        // TODO: buscar avaliação por id
        return Optional.empty();
    }

    @Override
    public List<Avaliacao> listarPorPeriodo(Instant inicio, Instant fim) {
        // TODO: listar avaliações no período informado
        return List.of();
    }

}
