package br.com.fiap.techchallenge.fase4.feedback.infrastructure.persistence;

import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.domain.enums.Urgencia;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistência de avaliações no Amazon DynamoDB.
 */
public class DynamoDbAvaliacaoRepository implements AvaliacaoRepository {

    private final DynamoDbTable<AvaliacaoEntity> tabela;

    public DynamoDbAvaliacaoRepository(DynamoDbTable<AvaliacaoEntity> tabela) {
        this.tabela = tabela;
    }

    private AvaliacaoEntity toEntity(Avaliacao avaliacao) {
        AvaliacaoEntity entidade = new AvaliacaoEntity();
        entidade.setId(avaliacao.getId());
        entidade.setDescricao(avaliacao.getDescricao());
        entidade.setNota(avaliacao.getNota());
        entidade.setUrgencia(avaliacao.getUrgencia().name());
        entidade.setDataEnvio(avaliacao.getDataEnvio().toString());
        return entidade;
    }

    private Avaliacao toDomain(AvaliacaoEntity entidade) {
        return new Avaliacao(
                entidade.getId(),
                entidade.getDescricao(),
                entidade.getNota(),
                Urgencia.valueOf(entidade.getUrgencia()),
                Instant.parse(entidade.getDataEnvio())
        );
    }

    @Override
    public Avaliacao salvar(Avaliacao avaliacao) {
        // TODO: persistir avaliação no DynamoDB
        AvaliacaoEntity entidade = toEntity(avaliacao);
        tabela.putItem(entidade);
        return toDomain(entidade);
    }

    @Override
    public Optional<Avaliacao> buscarPorId(String id) {
        // TODO: buscar avaliação por id
        AvaliacaoEntity entidade = tabela.getItem(
                Key.builder().partitionValue(id).build()
        );

        return Optional.ofNullable(entidade)
                .map(this::toDomain);
    }

    @Override
    public List<Avaliacao> listarPorPeriodo(Instant inicio, Instant fim) {
        // TODO: listar avaliações no período informado
        return tabela.scan()
                .items()
                .stream()
                .map(this::toDomain)
                .filter(avaliacao ->
                        !avaliacao.getDataEnvio().isBefore(inicio)
                                && !avaliacao.getDataEnvio().isAfter(fim))
                .toList();
    }
}
