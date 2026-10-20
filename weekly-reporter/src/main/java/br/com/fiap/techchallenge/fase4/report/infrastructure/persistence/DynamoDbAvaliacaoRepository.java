package br.com.fiap.techchallenge.fase4.report.infrastructure.persistence;

import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de leitura de avaliações no DynamoDB para o relatório semanal.
 */
public class DynamoDbAvaliacaoRepository implements AvaliacaoRepository {

    @Override
    public Avaliacao salvar(Avaliacao avaliacao) {
        // TODO: não utilizado neste módulo (somente leitura)
        return null;
    }

    @Override
    public Optional<Avaliacao> buscarPorId(String id) {
        // TODO: buscar avaliação por id, se necessário
        return Optional.empty();
    }

    @Override
    public List<Avaliacao> listarPorPeriodo(Instant inicio, Instant fim) {
        // TODO: consultar avaliações da semana
        return List.of();
    }

}
