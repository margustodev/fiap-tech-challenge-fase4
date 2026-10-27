package br.com.fiap.techchallenge.fase4.infrastructure.persistence;

import br.com.fiap.techchallenge.fase4.application.port.out.AvaliacaoRepository;
import br.com.fiap.techchallenge.fase4.domain.model.Avaliacao;
import br.com.fiap.techchallenge.fase4.infrastructure.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de persistência de avaliações no Amazon DynamoDB.
 * Escrita por PK {@code id}; leitura por período via GSI {@code periodo-index}.
 */
public class DynamoDbAvaliacaoRepository implements AvaliacaoRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbAvaliacaoRepository.class);

    private final DynamoDbTable<AvaliacaoEntity> table;
    private final DynamoDbIndex<AvaliacaoEntity> periodoIndex;
    private final AvaliacaoEntityMapper mapper;

    public DynamoDbAvaliacaoRepository(DynamoDbEnhancedClient enhancedClient, AppConfig appConfig) {
        this(enhancedClient, appConfig, new AvaliacaoEntityMapper());
    }

    public DynamoDbAvaliacaoRepository(
            DynamoDbEnhancedClient enhancedClient,
            AppConfig appConfig,
            AvaliacaoEntityMapper mapper) {
        Objects.requireNonNull(enhancedClient, "enhancedClient");
        Objects.requireNonNull(appConfig, "appConfig");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        String tableName = appConfig.getNomeTabelaAvaliacoes();
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(AvaliacaoEntity.class));
        this.periodoIndex = table.index(AvaliacoesTableSchema.PERIODO_INDEX);
        LOGGER.info("Repositório DynamoDB inicializado para tabela {}", tableName);
    }

    DynamoDbAvaliacaoRepository(
            DynamoDbTable<AvaliacaoEntity> table,
            DynamoDbIndex<AvaliacaoEntity> periodoIndex,
            AvaliacaoEntityMapper mapper) {
        this.table = table;
        this.periodoIndex = periodoIndex;
        this.mapper = mapper;
    }

    @Override
    public Avaliacao salvar(Avaliacao avaliacao) {
        Objects.requireNonNull(avaliacao, "avaliacao");
        Objects.requireNonNull(avaliacao.getId(), "avaliacao.id");

        AvaliacaoEntity entity = mapper.toEntity(avaliacao);
        table.putItem(entity);
        LOGGER.debug("Avaliação persistida id={}", entity.getId());
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Avaliacao> buscarPorId(String id) {
        Objects.requireNonNull(id, "id");

        AvaliacaoEntity entity = table.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(mapper.toDomain(entity));
    }

    @Override
    public List<Avaliacao> listarPorPeriodo(Instant inicio, Instant fim) {
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fim, "fim");
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("fim deve ser maior ou igual a inicio");
        }

        QueryConditional conditional = QueryConditional.sortBetween(
                Key.builder()
                        .partitionValue(AvaliacoesTableSchema.ENTITY_TYPE_AVALIACAO)
                        .sortValue(inicio.toString())
                        .build(),
                Key.builder()
                        .partitionValue(AvaliacoesTableSchema.ENTITY_TYPE_AVALIACAO)
                        .sortValue(fim.toString())
                        .build()
        );

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(conditional)
                .build();

        List<Avaliacao> resultado = new ArrayList<>();
        periodoIndex.query(request).stream()
                .flatMap(page -> page.items().stream())
                .map(mapper::toDomain)
                .forEach(resultado::add);

        LOGGER.debug("Listagem por período {} a {}: {} itens", inicio, fim, resultado.size());
        return resultado;
    }

}
